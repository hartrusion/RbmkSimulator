/*
 * Copyright (C) 2026 Viktor Alexander Hartung
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

import java.awt.geom.AffineTransform;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Year;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * Converts the mnemonic SVG drawings into Java source code that draws the
 * same graphics with Java2D. Each SVG file in the directory results in a
 * class named after the SVG file with the suffix {@value #CLASS_NAME_SUFFIX}
 * that extends MnemonicBackground. The source files are written into the same
 * directory, they have to be copied into the project manually.
 *
 * <p>
 * Run it from this directory with {@code java MnemonicSvgConverter.java} or
 * pass the directory containing the SVG files as first argument. Only the JDK
 * is required.
 *
 * <p>
 * Only the subset of SVG that is used for the mnemonic drawings is
 * supported: Layers and groups, paths with straight lines and cubic bezier
 * curves, circles, ellipses, rectangles without rounded corners and Inkscape
 * arcs. Hidden layers and elements are skipped, they contain helper elements
 * for positioning the components in the GUI builder. Anything else, like text
 * or transparency, aborts the conversion of the file with an error message
 * instead of creating a drawing that differs from the SVG.
 *
 * <p>
 * All coordinates are converted to pixels of the exported drawing size,
 * transformations are applied to the coordinates. Line caps, line joins and
 * the miter limit are taken as they are defined in the SVG.
 *
 * @author Viktor Alexander Hartung
 */
public class MnemonicSvgConverter {

    private static final String SVG_NAMESPACE = "http://www.w3.org/2000/svg";

    private static final String INKSCAPE_NAMESPACE
            = "http://www.inkscape.org/namespaces/inkscape";

    private static final String SODIPODI_NAMESPACE
            = "http://sodipodi.sourceforge.net/DTD/sodipodi-0.dtd";

    /**
     * Package of the generated classes, which is the package of the
     * MnemonicBackground class.
     */
    private static final String TARGET_PACKAGE
            = "com.hartrusion.rbmksim.gui.mnemonic";

    private static final String CLASS_NAME_SUFFIX = "Background";

    private static final String COPYRIGHT_HOLDER = "Viktor Alexander Hartung";

    /**
     * Miter limit that is used by SVG if none is specified. Java uses 10 as
     * default, so the value is always passed to the BasicStroke.
     */
    private static final double SVG_DEFAULT_MITER_LIMIT = 4.0;

    /**
     * Number of decimal places for the coordinates in the generated code,
     * which is more than enough for pixel coordinates.
     */
    private static final int COORDINATE_DECIMALS = 3;

    /**
     * Tolerance for comparing transformation and scale values.
     */
    private static final double TOLERANCE = 1e-6;

    /**
     * Maximum line length of the generated code before argument lists get
     * wrapped into the next line.
     */
    private static final int MAX_LINE_LENGTH = 80;

    /**
     * Indentation of statements inside the drawBackground method.
     */
    private static final String STATEMENT_INDENT = "        ";

    /**
     * Additional indentation for wrapped argument lines.
     */
    private static final String CONTINUATION_INDENT = "        ";

    /**
     * SVG colors that may be used in the drawings, mapped to the constant in
     * MnemonicColors that represents the same color. Each color has to be
     * defined in MnemonicColors to keep the colors consistent with the
     * components drawn on the mnemonic displays.
     */
    private static final Map<String, String> COLOR_CONSTANTS = Map.of(
            "#5f6258", "MnemonicColors.PASSIVE",
            "#0edec2", "MnemonicColors.ACTIVE",
            "#383b3a", "MnemonicColors.BACKGROUND",
            "#5d855e", "MnemonicColors.DIM",
            "#4d451b", "MnemonicColors.READING_BACKGROUND",
            "#e7ffa6", "MnemonicColors.READING_TEXT");

    /**
     * Style properties that are inherited from groups and layers to their
     * child elements.
     */
    private static final List<String> INHERITED_PROPERTIES = List.of(
            "fill", "fill-opacity", "stroke", "stroke-width",
            "stroke-linecap", "stroke-linejoin", "stroke-miterlimit",
            "stroke-dasharray", "stroke-opacity");

    /**
     * Style properties which can also be given as XML attributes (SVG
     * presentation attributes) instead of inside the style attribute.
     */
    private static final List<String> PRESENTATION_ATTRIBUTES = List.of(
            "fill", "fill-opacity", "stroke", "stroke-width",
            "stroke-linecap", "stroke-linejoin", "stroke-miterlimit",
            "stroke-dasharray", "stroke-opacity", "opacity", "display");

    /**
     * Matches either a command letter or a number of SVG path data.
     */
    private static final Pattern PATH_TOKEN = Pattern.compile(
            "[A-Za-z]|[-+]?(?:\\d+\\.?\\d*|\\.\\d+)(?:[eE][-+]?\\d+)?");

    /**
     * Matches a single transform function like translate(1,2).
     */
    private static final Pattern TRANSFORM_FUNCTION = Pattern.compile(
            "([a-zA-Z]+)\\s*\\(([^)]*)\\)");

    /**
     * Matches a number inside a transform function or a length attribute.
     */
    private static final Pattern NUMBER = Pattern.compile(
            "[-+]?(?:\\d+\\.?\\d*|\\.\\d+)(?:[eE][-+]?\\d+)?");

    /**
     * Thrown if the SVG contains elements or properties that can not be
     * converted, the conversion of that file is aborted.
     */
    private static class UnsupportedSvgException extends Exception {

        UnsupportedSvgException(String message) {
            super(message);
        }
    }

    /**
     * One segment of a path in pixel coordinates. The type is one of the
     * absolute SVG commands M, L, C or Z, the points array contains the x and
     * y coordinates of the end point, preceded by the control points for C.
     */
    private static class PathSegment {

        final char type;
        final double[] points;

        PathSegment(char type, double[] points) {
            this.type = type;
            this.points = points;
        }
    }

    private final Path svgFile;

    private final String className;

    /**
     * Generated statements of the drawBackground method, each entry is one
     * complete line including the indentation.
     */
    private final List<String> statements = new ArrayList<>();

    /**
     * Stroke constants of the generated class, mapped from the constant name
     * to the initializer expression.
     */
    private final Map<String, String> strokeConstants = new LinkedHashMap<>();

    /**
     * Classes that need to be imported by the generated class.
     */
    private final TreeSet<String> imports = new TreeSet<>();

    /**
     * Color expression that was set by the last generated setColor call. Used
     * to only generate a setColor call if the color changes.
     */
    private String currentColor;

    /**
     * Stroke constant that was set by the last generated setStroke call.
     */
    private String currentStroke;

    /**
     * Set to true as soon as a statement uses the path variable, which then
     * gets declared at the beginning of the drawBackground method.
     */
    private boolean pathVariableUsed;

    private int drawingWidth;

    private int drawingHeight;

    public MnemonicSvgConverter(Path svgFile, String className) {
        this.svgFile = svgFile;
        this.className = className;
    }

    public static void main(String[] args) throws IOException {
        Path directory = Path.of(".");
        if (args.length > 0) {
            directory = Path.of(args[0]);
        }

        List<Path> svgFiles;
        try (Stream<Path> files = Files.list(directory)) {
            svgFiles = files
                    .filter(file -> file.getFileName().toString()
                    .toLowerCase(Locale.ROOT).endsWith(".svg"))
                    .sorted()
                    .collect(Collectors.toList());
        }
        if (svgFiles.isEmpty()) {
            System.err.println("No SVG files found in "
                    + directory.toAbsolutePath().normalize());
            System.exit(1);
        }

        boolean conversionFailed = false;
        for (Path svgFile : svgFiles) {
            String fileName = svgFile.getFileName().toString();
            String className = fileName.substring(0, fileName.length() - 4)
                    + CLASS_NAME_SUFFIX;
            Path javaFile = directory.resolve(className + ".java");
            try {
                String source = new MnemonicSvgConverter(svgFile, className)
                        .convert();
                Files.writeString(javaFile, source);
                System.out.println(fileName + " -> " + javaFile.getFileName());
            } catch (UnsupportedSvgException ex) {
                System.err.println(fileName + ": " + ex.getMessage());
                conversionFailed = true;
            } catch (Exception ex) {
                System.err.println(fileName + ": " + ex);
                conversionFailed = true;
            }
        }
        if (conversionFailed) {
            System.exit(1);
        }
    }

    /**
     * Reads the SVG file and creates the source code of the Java class.
     *
     * @return Complete content of the Java source file.
     * @throws Exception If the file can not be read, parsed or converted.
     */
    public String convert() throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        // SVG files from Inkscape do not need a DTD, prohibiting it prevents
        // any external entities from being resolved.
        factory.setFeature(
                "http://apache.org/xml/features/disallow-doctype-decl", true);
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document document = builder.parse(svgFile.toFile());
        Element root = document.getDocumentElement();

        AffineTransform pixelTransform = createPixelTransform(root);
        convertChildren(root, pixelTransform, new HashMap<>(), true);
        return createSource();
    }

    /**
     * Creates the transformation from the SVG user units of the viewBox to
     * pixels of the exported image, which has the size of the width and
     * height attributes.
     *
     * @param root The svg element.
     * @return Transformation from user units to pixels.
     * @throws UnsupportedSvgException If the size is not given in pixels or
     * the viewBox would distort the drawing.
     */
    private AffineTransform createPixelTransform(Element root)
            throws UnsupportedSvgException {
        double width = parsePixelLength(root.getAttribute("width"), "width");
        double height = parsePixelLength(root.getAttribute("height"),
                "height");
        drawingWidth = (int) Math.round(width);
        drawingHeight = (int) Math.round(height);
        if (Math.abs(width - drawingWidth) > TOLERANCE
                || Math.abs(height - drawingHeight) > TOLERANCE) {
            throw new UnsupportedSvgException(
                    "Width and height must be whole pixels.");
        }

        String viewBox = root.getAttribute("viewBox");
        if (viewBox.isEmpty()) {
            return new AffineTransform();
        }
        double[] viewBoxValues = parseNumbers(viewBox);
        if (viewBoxValues.length != 4) {
            throw new UnsupportedSvgException("Invalid viewBox: " + viewBox);
        }
        double scaleX = width / viewBoxValues[2];
        double scaleY = height / viewBoxValues[3];
        if (Math.abs(scaleX - scaleY) > TOLERANCE * scaleX) {
            throw new UnsupportedSvgException("The viewBox has a different "
                    + "aspect ratio than width and height.");
        }
        AffineTransform transform = AffineTransform.getScaleInstance(
                scaleX, scaleY);
        transform.translate(-viewBoxValues[0], -viewBoxValues[1]);
        return transform;
    }

    private static double parsePixelLength(String value, String name)
            throws UnsupportedSvgException {
        String trimmed = value.trim();
        if (trimmed.endsWith("px")) {
            trimmed = trimmed.substring(0, trimmed.length() - 2).trim();
        }
        if (!NUMBER.matcher(trimmed).matches()) {
            throw new UnsupportedSvgException("The " + name
                    + " of the svg element must be given in pixels, found \""
                    + value + "\".");
        }
        return Double.parseDouble(trimmed);
    }

    /**
     * Converts all child elements of a group or the svg root element.
     *
     * @param parent Element containing the children to convert.
     * @param parentTransform Transformation from the coordinate system of
     * the parent to pixels.
     * @param parentStyle Style properties that are inherited from the parent.
     * @param isRoot True if the parent is the svg element, its child groups
     * are the Inkscape layers.
     * @throws UnsupportedSvgException If any of the elements is not
     * supported.
     */
    private void convertChildren(Element parent,
            AffineTransform parentTransform, Map<String, String> parentStyle,
            boolean isRoot) throws UnsupportedSvgException {
        for (Node node = parent.getFirstChild(); node != null;
                node = node.getNextSibling()) {
            if (node.getNodeType() != Node.ELEMENT_NODE
                    || !SVG_NAMESPACE.equals(node.getNamespaceURI())) {
                // Text, comments and the Inkscape and Sodipodi elements like
                // namedview do not contain any drawing.
                continue;
            }
            Element element = (Element) node;
            Map<String, String> style = resolveStyle(element, parentStyle);
            if ("none".equals(style.get("display"))) {
                continue;
            }
            AffineTransform transform = new AffineTransform(parentTransform);
            if (element.hasAttribute("transform")) {
                transform.concatenate(
                        parseTransform(element.getAttribute("transform")));
            }

            switch (element.getLocalName()) {
                case "g":
                    if (isRoot && "layer".equals(element.getAttributeNS(
                            INKSCAPE_NAMESPACE, "groupmode"))) {
                        addBlankLine();
                        statements.add(STATEMENT_INDENT + "// Layer \""
                                + element.getAttributeNS(INKSCAPE_NAMESPACE,
                                        "label") + "\"");
                    }
                    convertChildren(element, transform, style, false);
                    break;
                case "path":
                    if ("arc".equals(element.getAttributeNS(
                            SODIPODI_NAMESPACE, "type"))) {
                        convertInkscapeArc(element, transform, style);
                    } else {
                        convertPath(element, transform, style);
                    }
                    break;
                case "circle":
                case "ellipse":
                    convertEllipse(element, transform, style);
                    break;
                case "rect":
                    convertRect(element, transform, style);
                    break;
                case "defs":
                case "title":
                case "desc":
                case "metadata":
                    break;
                default:
                    throw new UnsupportedSvgException("Element <"
                            + element.getLocalName() + "> with id \""
                            + element.getAttribute("id")
                            + "\" is not supported.");
            }
        }
    }

    /**
     * Determines the style properties of an element by combining the
     * inherited properties of the parent, the presentation attributes and
     * the style attribute of the element, in this order of priority.
     *
     * @param element The element.
     * @param parentStyle Resolved style of the parent element.
     * @return Map of property names to values.
     */
    private static Map<String, String> resolveStyle(Element element,
            Map<String, String> parentStyle) {
        Map<String, String> style = new HashMap<>();
        for (String property : INHERITED_PROPERTIES) {
            if (parentStyle.containsKey(property)) {
                style.put(property, parentStyle.get(property));
            }
        }
        for (String property : PRESENTATION_ATTRIBUTES) {
            if (element.hasAttribute(property)) {
                style.put(property, element.getAttribute(property).trim());
            }
        }
        for (String declaration : element.getAttribute("style").split(";")) {
            int separator = declaration.indexOf(':');
            if (separator > 0) {
                style.put(declaration.substring(0, separator).trim(),
                        declaration.substring(separator + 1).trim());
            }
        }
        return style;
    }

    /**
     * Parses the value of a transform attribute.
     *
     * @param value List of transform functions.
     * @return The resulting transformation.
     * @throws UnsupportedSvgException On unknown or invalid functions.
     */
    private static AffineTransform parseTransform(String value)
            throws UnsupportedSvgException {
        AffineTransform transform = new AffineTransform();
        Matcher matcher = TRANSFORM_FUNCTION.matcher(value);
        while (matcher.find()) {
            String function = matcher.group(1);
            double[] arguments = parseNumbers(matcher.group(2));
            switch (function) {
                case "matrix":
                    requireArgumentCount(function, arguments, 6, 6);
                    transform.concatenate(new AffineTransform(arguments));
                    break;
                case "translate":
                    requireArgumentCount(function, arguments, 1, 2);
                    if (arguments.length == 1) {
                        transform.translate(arguments[0], 0);
                    } else {
                        transform.translate(arguments[0], arguments[1]);
                    }
                    break;
                case "scale":
                    requireArgumentCount(function, arguments, 1, 2);
                    if (arguments.length == 1) {
                        transform.scale(arguments[0], arguments[0]);
                    } else {
                        transform.scale(arguments[0], arguments[1]);
                    }
                    break;
                case "rotate":
                    requireArgumentCount(function, arguments, 1, 3);
                    if (arguments.length == 3) {
                        transform.rotate(Math.toRadians(arguments[0]),
                                arguments[1], arguments[2]);
                    } else if (arguments.length == 1) {
                        transform.rotate(Math.toRadians(arguments[0]));
                    } else {
                        throw new UnsupportedSvgException(
                                "Invalid rotate transformation: " + value);
                    }
                    break;
                default:
                    throw new UnsupportedSvgException("Transformation "
                            + function + " is not supported.");
            }
        }
        return transform;
    }

    private static void requireArgumentCount(String function,
            double[] arguments, int min, int max)
            throws UnsupportedSvgException {
        if (arguments.length < min || arguments.length > max) {
            throw new UnsupportedSvgException("Invalid number of arguments "
                    + "for transformation " + function + ".");
        }
    }

    private static double[] parseNumbers(String value) {
        Matcher matcher = NUMBER.matcher(value);
        List<Double> numbers = new ArrayList<>();
        while (matcher.find()) {
            numbers.add(Double.parseDouble(matcher.group()));
        }
        double[] result = new double[numbers.size()];
        for (int i = 0; i < result.length; i++) {
            result[i] = numbers.get(i);
        }
        return result;
    }

    /**
     * Converts a path element. A path that consists of a single sequence of
     * straight lines is drawn with the polyline and polygon methods of
     * MnemonicBackground, all other paths are built with Path2D.
     *
     * @param element The path element.
     * @param transform Transformation from the element coordinates to pixels.
     * @param style Resolved style of the element.
     * @throws UnsupportedSvgException If the path data or style is not
     * supported.
     */
    private void convertPath(Element element, AffineTransform transform,
            Map<String, String> style) throws UnsupportedSvgException {
        String id = element.getAttribute("id");
        List<PathSegment> segments = parsePathData(
                element.getAttribute("d"), transform, id);
        if (segments.isEmpty()) {
            return;
        }

        int subpathCount = 0;
        boolean onlyLines = true;
        for (PathSegment segment : segments) {
            if (segment.type == 'M') {
                subpathCount++;
            } else if (segment.type == 'C') {
                onlyLines = false;
            }
        }
        boolean closed = segments.get(segments.size() - 1).type == 'Z';
        boolean simplePolyline = onlyLines && subpathCount == 1;

        List<String> coordinates = new ArrayList<>();
        if (simplePolyline) {
            for (PathSegment segment : segments) {
                for (double value : segment.points) {
                    coordinates.add(formatNumber(value));
                }
            }
        } else {
            createPathStatements(segments);
        }

        if (setFillPaint(style, id)) {
            if (simplePolyline) {
                addCall("fillPolygon(g2d, ", coordinates, ");", id);
            } else {
                statements.add(STATEMENT_INDENT + "g2d.fill(path); // " + id);
            }
        }
        if (setStrokePaint(style, transform, id)) {
            if (simplePolyline && closed) {
                addCall("drawPolygon(g2d, ", coordinates, ");", id);
            } else if (simplePolyline) {
                addCall("drawPolyline(g2d, ", coordinates, ");", id);
            } else {
                statements.add(STATEMENT_INDENT + "g2d.draw(path); // " + id);
            }
        }
    }

    /**
     * Parses SVG path data into segments with absolute pixel coordinates.
     * Relative commands and the horizontal and vertical line commands are
     * converted to absolute M, L, C and Z segments.
     *
     * @param data Value of the d attribute.
     * @param transform Transformation from the path coordinates to pixels.
     * @param id Element id for error messages.
     * @return List of segments.
     * @throws UnsupportedSvgException If unsupported commands are used.
     */
    private static List<PathSegment> parsePathData(String data,
            AffineTransform transform, String id)
            throws UnsupportedSvgException {
        List<String> tokens = new ArrayList<>();
        Matcher matcher = PATH_TOKEN.matcher(data);
        while (matcher.find()) {
            tokens.add(matcher.group());
        }

        List<PathSegment> segments = new ArrayList<>();
        // Current point and start point of the current subpath in the
        // coordinate system of the path element.
        double x = 0;
        double y = 0;
        double startX = 0;
        double startY = 0;
        char command = ' ';
        int index = 0;
        while (index < tokens.size()) {
            String token = tokens.get(index);
            if (Character.isLetter(token.charAt(0))) {
                command = token.charAt(0);
                index++;
            } else if (command == ' ') {
                throw new UnsupportedSvgException("Path \"" + id
                        + "\" does not start with a command.");
            }
            boolean relative = Character.isLowerCase(command);
            double offsetX = 0;
            double offsetY = 0;
            if (relative) {
                offsetX = x;
                offsetY = y;
            }
            switch (Character.toUpperCase(command)) {
                case 'M':
                    x = offsetX + parseToken(tokens, index++, id);
                    y = offsetY + parseToken(tokens, index++, id);
                    startX = x;
                    startY = y;
                    segments.add(new PathSegment('M',
                            transformPoints(transform, x, y)));
                    // Further coordinate pairs after a moveto are implicit
                    // lineto commands.
                    if (relative) {
                        command = 'l';
                    } else {
                        command = 'L';
                    }
                    break;
                case 'L':
                    x = offsetX + parseToken(tokens, index++, id);
                    y = offsetY + parseToken(tokens, index++, id);
                    segments.add(new PathSegment('L',
                            transformPoints(transform, x, y)));
                    break;
                case 'H':
                    x = offsetX + parseToken(tokens, index++, id);
                    segments.add(new PathSegment('L',
                            transformPoints(transform, x, y)));
                    break;
                case 'V':
                    y = offsetY + parseToken(tokens, index++, id);
                    segments.add(new PathSegment('L',
                            transformPoints(transform, x, y)));
                    break;
                case 'C':
                    double control1X = offsetX
                            + parseToken(tokens, index++, id);
                    double control1Y = offsetY
                            + parseToken(tokens, index++, id);
                    double control2X = offsetX
                            + parseToken(tokens, index++, id);
                    double control2Y = offsetY
                            + parseToken(tokens, index++, id);
                    x = offsetX + parseToken(tokens, index++, id);
                    y = offsetY + parseToken(tokens, index++, id);
                    segments.add(new PathSegment('C', transformPoints(
                            transform, control1X, control1Y,
                            control2X, control2Y, x, y)));
                    break;
                case 'Z':
                    x = startX;
                    y = startY;
                    segments.add(new PathSegment('Z', new double[0]));
                    break;
                default:
                    throw new UnsupportedSvgException("Path \"" + id
                            + "\" uses the command " + command
                            + " which is not supported.");
            }
        }
        return segments;
    }

    private static double parseToken(List<String> tokens, int index,
            String id) throws UnsupportedSvgException {
        if (index >= tokens.size()
                || Character.isLetter(tokens.get(index).charAt(0))) {
            throw new UnsupportedSvgException("Path \"" + id
                    + "\" has missing coordinates.");
        }
        return Double.parseDouble(tokens.get(index));
    }

    private static double[] transformPoints(AffineTransform transform,
            double... points) {
        double[] result = new double[points.length];
        transform.transform(points, 0, result, 0, points.length / 2);
        return result;
    }

    /**
     * Generates the statements that build the path variable from the
     * segments.
     *
     * @param segments Path segments in pixel coordinates.
     */
    private void createPathStatements(List<PathSegment> segments) {
        pathVariableUsed = true;
        imports.add("java.awt.geom.Path2D");
        statements.add(STATEMENT_INDENT + "path = new Path2D.Double();");
        for (PathSegment segment : segments) {
            List<String> arguments = new ArrayList<>();
            for (double value : segment.points) {
                arguments.add(formatNumber(value));
            }
            switch (segment.type) {
                case 'M':
                    addCall("path.moveTo(", arguments, ");", null);
                    break;
                case 'L':
                    addCall("path.lineTo(", arguments, ");", null);
                    break;
                case 'C':
                    addCall("path.curveTo(", arguments, ");", null);
                    break;
                default:
                    statements.add(STATEMENT_INDENT + "path.closePath();");
                    break;
            }
        }
    }

    /**
     * Converts a circle or ellipse element.
     *
     * @param element The circle or ellipse element.
     * @param transform Transformation from the element coordinates to pixels.
     * @param style Resolved style of the element.
     * @throws UnsupportedSvgException If the transformation would rotate or
     * skew an ellipse or the style is not supported.
     */
    private void convertEllipse(Element element, AffineTransform transform,
            Map<String, String> style) throws UnsupportedSvgException {
        String id = element.getAttribute("id");
        double radiusX;
        double radiusY;
        if ("circle".equals(element.getLocalName())) {
            radiusX = parseAttribute(element, "r");
            radiusY = radiusX;
        } else {
            radiusX = parseAttribute(element, "rx");
            radiusY = parseAttribute(element, "ry");
        }
        double[] center = transformPoints(transform,
                parseAttribute(element, "cx"), parseAttribute(element, "cy"));
        double[] radii = getTransformedRadii(transform, radiusX, radiusY, id);

        imports.add("java.awt.geom.Ellipse2D");
        List<String> arguments = List.of(
                formatNumber(center[0] - radii[0]),
                formatNumber(center[1] - radii[1]),
                formatNumber(2 * radii[0]), formatNumber(2 * radii[1]));
        if (setFillPaint(style, id)) {
            addCall("g2d.fill(new Ellipse2D.Double(", arguments, "));", id);
        }
        if (setStrokePaint(style, transform, id)) {
            addCall("g2d.draw(new Ellipse2D.Double(", arguments, "));", id);
        }
    }

    /**
     * Converts an arc that was drawn with the Inkscape circle tool. Inkscape
     * stores the exact arc parameters in its own attributes, they are used
     * instead of the path data, which is only an approximation.
     *
     * @param element The path element with sodipodi:type="arc".
     * @param transform Transformation from the element coordinates to pixels.
     * @param style Resolved style of the element.
     * @throws UnsupportedSvgException If the transformation would rotate,
     * mirror or skew the arc or the style is not supported.
     */
    private void convertInkscapeArc(Element element,
            AffineTransform transform, Map<String, String> style)
            throws UnsupportedSvgException {
        String id = element.getAttribute("id");
        if (Math.abs(transform.getShearX()) > TOLERANCE
                || Math.abs(transform.getShearY()) > TOLERANCE
                || transform.getScaleX() <= 0
                || transform.getScaleY() <= 0) {
            throw new UnsupportedSvgException("Arc \"" + id
                    + "\" is rotated or mirrored, which is not supported.");
        }
        double[] center = transformPoints(transform,
                parseSodipodiAttribute(element, "cx"),
                parseSodipodiAttribute(element, "cy"));
        double radiusX = parseSodipodiAttribute(element, "rx")
                * transform.getScaleX();
        double radiusY = parseSodipodiAttribute(element, "ry")
                * transform.getScaleY();

        // Inkscape measures the angles clockwise on the screen and draws the
        // arc from start to end with increasing angle, Arc2D measures them
        // counterclockwise.
        double start = 0;
        double end = 2 * Math.PI;
        if (element.hasAttributeNS(SODIPODI_NAMESPACE, "start")) {
            start = parseSodipodiAttribute(element, "start");
            end = parseSodipodiAttribute(element, "end");
        }
        if (end <= start) {
            end = end + 2 * Math.PI;
        }
        double javaStart = -Math.toDegrees(start);
        double javaExtent = -Math.toDegrees(end - start);

        String arcType;
        String inkscapeArcType = element.getAttributeNS(SODIPODI_NAMESPACE,
                "arc-type");
        if ("slice".equals(inkscapeArcType)) {
            arcType = "Arc2D.PIE";
        } else if ("chord".equals(inkscapeArcType)) {
            arcType = "Arc2D.CHORD";
        } else if ("arc".equals(inkscapeArcType)
                || "true".equals(element.getAttributeNS(SODIPODI_NAMESPACE,
                        "open"))) {
            arcType = "Arc2D.OPEN";
        } else {
            arcType = "Arc2D.PIE";
        }

        imports.add("java.awt.geom.Arc2D");
        List<String> arguments = List.of(
                formatNumber(center[0] - radiusX),
                formatNumber(center[1] - radiusY),
                formatNumber(2 * radiusX), formatNumber(2 * radiusY),
                formatNumber(javaStart), formatNumber(javaExtent), arcType);
        if (setFillPaint(style, id)) {
            addCall("g2d.fill(new Arc2D.Double(", arguments, "));", id);
        }
        if (setStrokePaint(style, transform, id)) {
            addCall("g2d.draw(new Arc2D.Double(", arguments, "));", id);
        }
    }

    /**
     * Converts a rectangle element into a polygon, which allows any
     * transformation of the rectangle.
     *
     * @param element The rect element.
     * @param transform Transformation from the element coordinates to pixels.
     * @param style Resolved style of the element.
     * @throws UnsupportedSvgException If the rectangle has rounded corners or
     * the style is not supported.
     */
    private void convertRect(Element element, AffineTransform transform,
            Map<String, String> style) throws UnsupportedSvgException {
        String id = element.getAttribute("id");
        if (element.hasAttribute("rx") || element.hasAttribute("ry")) {
            throw new UnsupportedSvgException("Rectangle \"" + id
                    + "\" has rounded corners, which is not supported.");
        }
        double x = 0;
        double y = 0;
        if (element.hasAttribute("x")) {
            x = parseAttribute(element, "x");
        }
        if (element.hasAttribute("y")) {
            y = parseAttribute(element, "y");
        }
        double width = parseAttribute(element, "width");
        double height = parseAttribute(element, "height");
        double[] corners = transformPoints(transform, x, y, x + width, y,
                x + width, y + height, x, y + height);
        List<String> coordinates = new ArrayList<>();
        for (double value : corners) {
            coordinates.add(formatNumber(value));
        }
        if (setFillPaint(style, id)) {
            addCall("fillPolygon(g2d, ", coordinates, ");", id);
        }
        if (setStrokePaint(style, transform, id)) {
            addCall("drawPolygon(g2d, ", coordinates, ");", id);
        }
    }

    private static double parseAttribute(Element element, String name)
            throws UnsupportedSvgException {
        String value = element.getAttribute(name).trim();
        if (!NUMBER.matcher(value).matches()) {
            throw new UnsupportedSvgException("Attribute " + name
                    + " of element \"" + element.getAttribute("id")
                    + "\" must be a plain number, found \"" + value + "\".");
        }
        return Double.parseDouble(value);
    }

    private static double parseSodipodiAttribute(Element element,
            String name) throws UnsupportedSvgException {
        String value = element.getAttributeNS(SODIPODI_NAMESPACE, name)
                .trim();
        if (!NUMBER.matcher(value).matches()) {
            throw new UnsupportedSvgException("Attribute sodipodi:" + name
                    + " of arc \"" + element.getAttribute("id")
                    + "\" must be a plain number, found \"" + value + "\".");
        }
        return Double.parseDouble(value);
    }

    /**
     * Calculates the radii of an ellipse in pixels. A circle may be rotated
     * or mirrored, an ellipse may only be mirrored.
     *
     * @return Array with the radius in x and y direction.
     */
    private static double[] getTransformedRadii(AffineTransform transform,
            double radiusX, double radiusY, String id)
            throws UnsupportedSvgException {
        boolean rotatedOrSkewed = Math.abs(transform.getShearX()) > TOLERANCE
                || Math.abs(transform.getShearY()) > TOLERANCE;
        if (Math.abs(radiusX - radiusY) < TOLERANCE) {
            if (!isUniformScaling(transform)) {
                throw new UnsupportedSvgException("Circle \"" + id
                        + "\" is distorted by its transformation.");
            }
            double scale = Math.sqrt(Math.abs(transform.getDeterminant()));
            return new double[]{radiusX * scale, radiusY * scale};
        }
        if (rotatedOrSkewed) {
            throw new UnsupportedSvgException("Ellipse \"" + id
                    + "\" is rotated or skewed, which is not supported.");
        }
        return new double[]{radiusX * Math.abs(transform.getScaleX()),
            radiusY * Math.abs(transform.getScaleY())};
    }

    /**
     * Checks that a transformation does not distort shapes, which means it
     * only consists of translation, rotation, mirroring and uniform scaling.
     */
    private static boolean isUniformScaling(AffineTransform transform) {
        double a = transform.getScaleX();
        double b = transform.getShearY();
        double c = transform.getShearX();
        double d = transform.getScaleY();
        double lengthX = a * a + b * b;
        double lengthY = c * c + d * d;
        return Math.abs(lengthX - lengthY) < TOLERANCE * lengthX
                && Math.abs(a * c + b * d) < TOLERANCE * lengthX;
    }

    /**
     * Generates a setColor call for the fill color if the element is filled.
     *
     * @return True if the element has to be filled.
     */
    private boolean setFillPaint(Map<String, String> style, String id)
            throws UnsupportedSvgException {
        checkOpacity(style, "opacity", id);
        // SVG fills with black if no fill is specified.
        String fill = style.getOrDefault("fill", "#000000");
        if ("none".equals(fill)) {
            return false;
        }
        checkOpacity(style, "fill-opacity", id);
        setColor(fill, id);
        return true;
    }

    /**
     * Generates setColor and setStroke calls if the element has a stroke.
     *
     * @return True if the element has a stroke to draw.
     */
    private boolean setStrokePaint(Map<String, String> style,
            AffineTransform transform, String id)
            throws UnsupportedSvgException {
        String stroke = style.getOrDefault("stroke", "none");
        if ("none".equals(stroke)) {
            return false;
        }
        checkOpacity(style, "stroke-opacity", id);
        String dashArray = style.getOrDefault("stroke-dasharray", "none");
        if (!"none".equals(dashArray)) {
            throw new UnsupportedSvgException("Element \"" + id
                    + "\" uses a dashed stroke, which is not supported.");
        }
        if (!isUniformScaling(transform)) {
            throw new UnsupportedSvgException("The stroke of element \"" + id
                    + "\" is distorted by its transformation.");
        }
        setColor(stroke, id);

        double width = parseStyleNumber(style, "stroke-width", 1.0, id)
                * Math.sqrt(Math.abs(transform.getDeterminant()));
        double miterLimit = parseStyleNumber(style, "stroke-miterlimit",
                SVG_DEFAULT_MITER_LIMIT, id);
        String cap = style.getOrDefault("stroke-linecap", "butt");
        String join = style.getOrDefault("stroke-linejoin", "miter");
        String javaCap;
        switch (cap) {
            case "butt":
                javaCap = "BasicStroke.CAP_BUTT";
                break;
            case "square":
                javaCap = "BasicStroke.CAP_SQUARE";
                break;
            case "round":
                javaCap = "BasicStroke.CAP_ROUND";
                break;
            default:
                throw new UnsupportedSvgException("Element \"" + id
                        + "\" uses the unknown line cap " + cap + ".");
        }
        String javaJoin;
        switch (join) {
            case "miter":
                javaJoin = "BasicStroke.JOIN_MITER";
                break;
            case "round":
                javaJoin = "BasicStroke.JOIN_ROUND";
                break;
            case "bevel":
                javaJoin = "BasicStroke.JOIN_BEVEL";
                break;
            default:
                throw new UnsupportedSvgException("Element \"" + id
                        + "\" uses the unknown line join " + join + ".");
        }

        String constantName = "STROKE_"
                + formatNumber(width).replace('.', '_')
                + "_" + cap.toUpperCase(Locale.ROOT)
                + "_" + join.toUpperCase(Locale.ROOT);
        if (Math.abs(miterLimit - SVG_DEFAULT_MITER_LIMIT) > TOLERANCE) {
            constantName = constantName + "_MITERLIMIT_"
                    + formatNumber(miterLimit).replace('.', '_');
        }
        strokeConstants.put(constantName, "new BasicStroke("
                + formatNumber(width) + "F, " + javaCap + ",\n"
                + "                    " + javaJoin + ", "
                + formatNumber(miterLimit) + "F)");
        if (!constantName.equals(currentStroke)) {
            statements.add(STATEMENT_INDENT + "g2d.setStroke(" + constantName
                    + ");");
            currentStroke = constantName;
        }
        return true;
    }

    private void setColor(String svgColor, String id)
            throws UnsupportedSvgException {
        String colorConstant = COLOR_CONSTANTS.get(
                svgColor.toLowerCase(Locale.ROOT));
        if (colorConstant == null) {
            throw new UnsupportedSvgException("Element \"" + id
                    + "\" uses the color " + svgColor + " which is not "
                    + "defined in MnemonicColors.");
        }
        if (!colorConstant.equals(currentColor)) {
            statements.add(STATEMENT_INDENT + "g2d.setColor(" + colorConstant
                    + ");");
            currentColor = colorConstant;
        }
    }

    private static void checkOpacity(Map<String, String> style,
            String property, String id) throws UnsupportedSvgException {
        if (parseStyleNumber(style, property, 1.0, id) < 1.0) {
            throw new UnsupportedSvgException("Element \"" + id
                    + "\" is transparent (" + property
                    + "), which is not supported.");
        }
    }

    private static double parseStyleNumber(Map<String, String> style,
            String property, double defaultValue, String id)
            throws UnsupportedSvgException {
        if (!style.containsKey(property)) {
            return defaultValue;
        }
        String value = style.get(property);
        if (value.endsWith("px")) {
            value = value.substring(0, value.length() - 2);
        }
        if (!NUMBER.matcher(value).matches()) {
            throw new UnsupportedSvgException("Property " + property
                    + " of element \"" + id + "\" has the unsupported value \""
                    + style.get(property) + "\".");
        }
        return Double.parseDouble(value);
    }

    /**
     * Formats a coordinate with a limited number of decimal places and
     * without trailing zeros.
     */
    private static String formatNumber(double value) {
        String text = String.format(Locale.ROOT, "%." + COORDINATE_DECIMALS
                + "f", value);
        if (text.contains(".")) {
            text = text.replaceAll("0+$", "");
            text = text.replaceAll("\\.$", "");
        }
        if ("-0".equals(text)) {
            text = "0";
        }
        return text;
    }

    private void addBlankLine() {
        if (!statements.isEmpty()
                && !statements.get(statements.size() - 1).isEmpty()) {
            statements.add("");
        }
    }

    /**
     * Adds a method call statement and wraps the argument list into
     * multiple lines if it would exceed the maximum line length.
     *
     * @param prefix Text before the arguments, like "path.moveTo(".
     * @param arguments Arguments of the call.
     * @param suffix Text after the arguments, like ");".
     * @param comment Comment to append to the statement, may be null.
     */
    private void addCall(String prefix, List<String> arguments,
            String suffix, String comment) {
        String commentText = "";
        if (comment != null && !comment.isEmpty()) {
            commentText = " // " + comment;
        }
        String singleLine = STATEMENT_INDENT + prefix
                + String.join(", ", arguments) + suffix;
        if (singleLine.length() + commentText.length() <= MAX_LINE_LENGTH) {
            statements.add(singleLine + commentText);
            return;
        }
        // The comment is placed on the first line, the arguments are filled
        // into the lines until the maximum length is reached.
        statements.add(STATEMENT_INDENT + prefix.stripTrailing()
                + commentText);
        StringBuilder line = new StringBuilder(STATEMENT_INDENT
                + CONTINUATION_INDENT);
        int lineStart = line.length();
        for (int i = 0; i < arguments.size(); i++) {
            String argument = arguments.get(i);
            if (i < arguments.size() - 1) {
                argument = argument + ",";
            } else {
                argument = argument + suffix;
            }
            int separatorLength = 0;
            if (line.length() > lineStart) {
                separatorLength = 1;
            }
            if (line.length() > lineStart && line.length() + separatorLength
                    + argument.length() > MAX_LINE_LENGTH) {
                statements.add(line.toString());
                line = new StringBuilder(STATEMENT_INDENT
                        + CONTINUATION_INDENT);
            } else if (line.length() > lineStart) {
                line.append(' ');
            }
            line.append(argument);
        }
        statements.add(line.toString());
    }

    /**
     * Assembles the complete Java source file of the generated class.
     *
     * @return Source code.
     */
    private String createSource() {
        StringBuilder source = new StringBuilder();
        source.append("/*\n")
                .append(" * Copyright (C) ").append(Year.now().getValue())
                .append(" ").append(COPYRIGHT_HOLDER).append("\n")
                .append(" *\n")
                .append(" * This program is free software: you can "
                        + "redistribute it and/or modify\n")
                .append(" * it under the terms of the GNU General Public "
                        + "License as published by\n")
                .append(" * the Free Software Foundation, either version 3 "
                        + "of the License, or\n")
                .append(" * (at your option) any later version.\n")
                .append(" *\n")
                .append(" * This program is distributed in the hope that it "
                        + "will be useful,\n")
                .append(" * but WITHOUT ANY WARRANTY; without even the "
                        + "implied warranty of\n")
                .append(" * MERCHANTABILITY or FITNESS FOR A PARTICULAR "
                        + "PURPOSE.  See the\n")
                .append(" * GNU General Public License for more details.\n")
                .append(" *\n")
                .append(" * You should have received a copy of the GNU "
                        + "General Public License\n")
                .append(" * along with this program.  If not, see "
                        + "<http://www.gnu.org/licenses/>.\n")
                .append(" */\n")
                .append("package ").append(TARGET_PACKAGE).append(";\n\n");

        imports.add("java.awt.BasicStroke");
        imports.add("java.awt.Graphics2D");
        for (String importedClass : imports) {
            source.append("import ").append(importedClass).append(";\n");
        }

        source.append("\n/**\n")
                .append(" * Background drawing of a mnemonic display, "
                        + "generated by MnemonicSvgConverter\n")
                .append(" * from ").append(svgFile.getFileName())
                .append(".\n")
                .append(" * Do not modify this file, modify the SVG file and "
                        + "run the converter again\n")
                .append(" * instead.\n")
                .append(" *\n")
                .append(" * @author ").append(COPYRIGHT_HOLDER).append("\n")
                .append(" */\n")
                .append("public class ").append(className)
                .append(" extends MnemonicBackground {\n");

        for (Map.Entry<String, String> stroke : strokeConstants.entrySet()) {
            source.append("\n    private static final BasicStroke ")
                    .append(stroke.getKey()).append("\n            = ")
                    .append(stroke.getValue()).append(";\n");
        }

        source.append("\n    public ").append(className).append("() {\n")
                .append("        super(").append(drawingWidth).append(", ")
                .append(drawingHeight).append(");\n")
                .append("    }\n\n")
                .append("    @Override\n")
                .append("    protected void drawBackground(Graphics2D g2d) "
                        + "{\n");
        if (pathVariableUsed) {
            source.append(STATEMENT_INDENT).append("Path2D.Double path;\n\n");
        }
        for (String statement : statements) {
            source.append(statement).append("\n");
        }
        source.append("    }\n")
                .append("}\n");
        return source.toString();
    }
}
