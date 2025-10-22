/*
 * Copyright (C) 2025 Viktor Alexander Hartung
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
package com.hartrusion.rbmksim.gui.elements;

import com.hartrusion.rbmksim.ChannelData;


/**
 *
 * @author Viktor Alexander Hartung
 */
public class DisplayCoreBoolean extends javax.swing.JPanel {

    /**
     * Creates new form DisplayCoreBoolean
     */
    public DisplayCoreBoolean() {
        initComponents();
        initComponentsProperties();
    }

    /**
     * Allows access to the jLabel fields using the core index numbers which are
     * displayed on the label
     *
     * @param idx First part, for 12-34 this would be 12
     * @param jdx Second part, for 12-34 this would be 34
     * @return Reference to jLabel object or null if it does not exist.
     */
    public javax.swing.JLabel getLabel(int idx, int jdx) {
        // Don't worry, this part was written using a script which was itself
        // written by an AI. The Copilot AI even refused to write this and wrote
        // a script to write the code.
        // <editor-fold defaultstate="collapsed" desc="Large switch lookup">
        switch (idx) {
            case 42:
                switch (jdx) {
                    case 29:
                        return jLabel1;
                    case 30:
                        return jLabel2;
                    case 31:
                        return jLabel3;
                    case 32:
                        return jLabel4;
                    case 33:
                        return jLabel5;
                    default:
                        return null;
                }
            case 41:
                switch (jdx) {
                    case 26:
                        return jLabel6;
                    case 27:
                        return jLabel7;
                    case 28:
                        return jLabel8;
                    case 29:
                        return jLabel9;
                    case 30:
                        return jLabel10;
                    case 31:
                        return jLabel11;
                    case 32:
                        return jLabel12;
                    case 33:
                        return jLabel13;
                    case 34:
                        return jLabel14;
                    case 35:
                        return jLabel15;
                    case 36:
                        return jLabel16;
                    default:
                        return null;
                }
            case 40:
                switch (jdx) {
                    case 24:
                        return jLabel17;
                    case 25:
                        return jLabel18;
                    case 26:
                        return jLabel19;
                    case 27:
                        return jLabel20;
                    case 28:
                        return jLabel21;
                    case 29:
                        return jLabel22;
                    case 30:
                        return jLabel23;
                    case 31:
                        return jLabel24;
                    case 32:
                        return jLabel25;
                    case 33:
                        return jLabel26;
                    case 34:
                        return jLabel27;
                    case 35:
                        return jLabel28;
                    case 36:
                        return jLabel29;
                    case 37:
                        return jLabel30;
                    case 38:
                        return jLabel31;
                    default:
                        return null;
                }
            case 39:
                switch (jdx) {
                    case 23:
                        return jLabel32;
                    case 24:
                        return jLabel33;
                    case 25:
                        return jLabel34;
                    case 26:
                        return jLabel35;
                    case 27:
                        return jLabel36;
                    case 28:
                        return jLabel37;
                    case 29:
                        return jLabel38;
                    case 30:
                        return jLabel39;
                    case 31:
                        return jLabel40;
                    case 32:
                        return jLabel41;
                    case 33:
                        return jLabel42;
                    case 34:
                        return jLabel43;
                    case 35:
                        return jLabel44;
                    case 36:
                        return jLabel45;
                    case 37:
                        return jLabel46;
                    case 38:
                        return jLabel47;
                    case 39:
                        return jLabel48;
                    default:
                        return null;
                }
            case 38:
                switch (jdx) {
                    case 22:
                        return jLabel49;
                    case 23:
                        return jLabel50;
                    case 24:
                        return jLabel51;
                    case 25:
                        return jLabel52;
                    case 26:
                        return jLabel53;
                    case 27:
                        return jLabel54;
                    case 28:
                        return jLabel55;
                    case 29:
                        return jLabel56;
                    case 30:
                        return jLabel57;
                    case 31:
                        return jLabel58;
                    case 32:
                        return jLabel59;
                    case 33:
                        return jLabel60;
                    case 34:
                        return jLabel61;
                    case 35:
                        return jLabel62;
                    case 36:
                        return jLabel63;
                    case 37:
                        return jLabel64;
                    case 38:
                        return jLabel65;
                    case 39:
                        return jLabel66;
                    case 40:
                        return jLabel67;
                    default:
                        return null;
                }
            case 37:
                switch (jdx) {
                    case 22:
                        return jLabel68;
                    case 23:
                        return jLabel69;
                    case 24:
                        return jLabel70;
                    case 25:
                        return jLabel71;
                    case 26:
                        return jLabel72;
                    case 27:
                        return jLabel73;
                    case 28:
                        return jLabel74;
                    case 29:
                        return jLabel75;
                    case 30:
                        return jLabel76;
                    case 31:
                        return jLabel77;
                    case 32:
                        return jLabel78;
                    case 33:
                        return jLabel79;
                    case 34:
                        return jLabel80;
                    case 35:
                        return jLabel81;
                    case 36:
                        return jLabel82;
                    case 37:
                        return jLabel83;
                    case 38:
                        return jLabel84;
                    case 39:
                        return jLabel85;
                    case 40:
                        return jLabel86;
                    default:
                        return null;
                }
            case 36:
                switch (jdx) {
                    case 21:
                        return jLabel87;
                    case 22:
                        return jLabel88;
                    case 23:
                        return jLabel89;
                    case 24:
                        return jLabel90;
                    case 25:
                        return jLabel91;
                    case 26:
                        return jLabel92;
                    case 27:
                        return jLabel93;
                    case 28:
                        return jLabel94;
                    case 29:
                        return jLabel95;
                    case 30:
                        return jLabel96;
                    case 31:
                        return jLabel97;
                    case 32:
                        return jLabel98;
                    case 33:
                        return jLabel99;
                    case 34:
                        return jLabel100;
                    case 35:
                        return jLabel101;
                    case 36:
                        return jLabel102;
                    case 37:
                        return jLabel103;
                    case 38:
                        return jLabel104;
                    case 39:
                        return jLabel105;
                    case 40:
                        return jLabel106;
                    case 41:
                        return jLabel107;
                    default:
                        return null;
                }
            case 35:
                switch (jdx) {
                    case 21:
                        return jLabel108;
                    case 22:
                        return jLabel109;
                    case 23:
                        return jLabel110;
                    case 24:
                        return jLabel111;
                    case 25:
                        return jLabel112;
                    case 26:
                        return jLabel113;
                    case 27:
                        return jLabel114;
                    case 28:
                        return jLabel115;
                    case 29:
                        return jLabel116;
                    case 30:
                        return jLabel117;
                    case 31:
                        return jLabel118;
                    case 32:
                        return jLabel119;
                    case 33:
                        return jLabel120;
                    case 34:
                        return jLabel121;
                    case 35:
                        return jLabel122;
                    case 36:
                        return jLabel123;
                    case 37:
                        return jLabel124;
                    case 38:
                        return jLabel125;
                    case 39:
                        return jLabel126;
                    case 40:
                        return jLabel127;
                    case 41:
                        return jLabel128;
                    default:
                        return null;
                }
            case 34:
                switch (jdx) {
                    case 21:
                        return jLabel129;
                    case 22:
                        return jLabel130;
                    case 23:
                        return jLabel131;
                    case 24:
                        return jLabel132;
                    case 25:
                        return jLabel133;
                    case 26:
                        return jLabel134;
                    case 27:
                        return jLabel135;
                    case 28:
                        return jLabel136;
                    case 29:
                        return jLabel137;
                    case 30:
                        return jLabel138;
                    case 31:
                        return jLabel139;
                    case 32:
                        return jLabel140;
                    case 33:
                        return jLabel141;
                    case 34:
                        return jLabel142;
                    case 35:
                        return jLabel143;
                    case 36:
                        return jLabel144;
                    case 37:
                        return jLabel145;
                    case 38:
                        return jLabel146;
                    case 39:
                        return jLabel147;
                    case 40:
                        return jLabel148;
                    case 41:
                        return jLabel149;
                    default:
                        return null;
                }
            case 33:
                switch (jdx) {
                    case 20:
                        return jLabel150;
                    case 21:
                        return jLabel151;
                    case 22:
                        return jLabel152;
                    case 23:
                        return jLabel153;
                    case 24:
                        return jLabel154;
                    case 25:
                        return jLabel155;
                    case 26:
                        return jLabel156;
                    case 27:
                        return jLabel157;
                    case 28:
                        return jLabel158;
                    case 29:
                        return jLabel159;
                    case 30:
                        return jLabel160;
                    case 31:
                        return jLabel161;
                    case 32:
                        return jLabel162;
                    case 33:
                        return jLabel163;
                    case 34:
                        return jLabel164;
                    case 35:
                        return jLabel165;
                    case 36:
                        return jLabel166;
                    case 37:
                        return jLabel167;
                    case 38:
                        return jLabel168;
                    case 39:
                        return jLabel169;
                    case 40:
                        return jLabel170;
                    case 41:
                        return jLabel171;
                    case 42:
                        return jLabel172;
                    default:
                        return null;
                }
            case 32:
                switch (jdx) {
                    case 20:
                        return jLabel173;
                    case 21:
                        return jLabel174;
                    case 22:
                        return jLabel175;
                    case 23:
                        return jLabel176;
                    case 24:
                        return jLabel177;
                    case 25:
                        return jLabel178;
                    case 26:
                        return jLabel179;
                    case 27:
                        return jLabel180;
                    case 28:
                        return jLabel181;
                    case 29:
                        return jLabel182;
                    case 30:
                        return jLabel183;
                    case 31:
                        return jLabel184;
                    case 32:
                        return jLabel185;
                    case 33:
                        return jLabel186;
                    case 34:
                        return jLabel187;
                    case 35:
                        return jLabel188;
                    case 36:
                        return jLabel189;
                    case 37:
                        return jLabel190;
                    case 38:
                        return jLabel191;
                    case 39:
                        return jLabel192;
                    case 40:
                        return jLabel193;
                    case 41:
                        return jLabel194;
                    case 42:
                        return jLabel195;
                    default:
                        return null;
                }
            case 31:
                switch (jdx) {
                    case 20:
                        return jLabel196;
                    case 21:
                        return jLabel197;
                    case 22:
                        return jLabel198;
                    case 23:
                        return jLabel199;
                    case 24:
                        return jLabel200;
                    case 25:
                        return jLabel201;
                    case 26:
                        return jLabel202;
                    case 27:
                        return jLabel203;
                    case 28:
                        return jLabel204;
                    case 29:
                        return jLabel205;
                    case 30:
                        return jLabel206;
                    case 31:
                        return jLabel207;
                    case 32:
                        return jLabel208;
                    case 33:
                        return jLabel209;
                    case 34:
                        return jLabel210;
                    case 35:
                        return jLabel211;
                    case 36:
                        return jLabel212;
                    case 37:
                        return jLabel213;
                    case 38:
                        return jLabel214;
                    case 39:
                        return jLabel215;
                    case 40:
                        return jLabel216;
                    case 41:
                        return jLabel217;
                    case 42:
                        return jLabel218;
                    default:
                        return null;
                }
            case 30:
                switch (jdx) {
                    case 20:
                        return jLabel219;
                    case 21:
                        return jLabel220;
                    case 22:
                        return jLabel221;
                    case 23:
                        return jLabel222;
                    case 24:
                        return jLabel223;
                    case 25:
                        return jLabel224;
                    case 26:
                        return jLabel225;
                    case 27:
                        return jLabel226;
                    case 28:
                        return jLabel227;
                    case 29:
                        return jLabel228;
                    case 30:
                        return jLabel229;
                    case 31:
                        return jLabel230;
                    case 32:
                        return jLabel231;
                    case 33:
                        return jLabel232;
                    case 34:
                        return jLabel233;
                    case 35:
                        return jLabel234;
                    case 36:
                        return jLabel235;
                    case 37:
                        return jLabel236;
                    case 38:
                        return jLabel237;
                    case 39:
                        return jLabel238;
                    case 40:
                        return jLabel239;
                    case 41:
                        return jLabel240;
                    case 42:
                        return jLabel241;
                    default:
                        return null;
                }
            case 29:
                switch (jdx) {
                    case 20:
                        return jLabel242;
                    case 21:
                        return jLabel243;
                    case 22:
                        return jLabel244;
                    case 23:
                        return jLabel245;
                    case 24:
                        return jLabel246;
                    case 25:
                        return jLabel247;
                    case 26:
                        return jLabel248;
                    case 27:
                        return jLabel249;
                    case 28:
                        return jLabel250;
                    case 29:
                        return jLabel251;
                    case 30:
                        return jLabel252;
                    case 31:
                        return jLabel253;
                    case 32:
                        return jLabel254;
                    case 33:
                        return jLabel255;
                    case 34:
                        return jLabel256;
                    case 35:
                        return jLabel257;
                    case 36:
                        return jLabel258;
                    case 37:
                        return jLabel259;
                    case 38:
                        return jLabel260;
                    case 39:
                        return jLabel261;
                    case 40:
                        return jLabel262;
                    case 41:
                        return jLabel263;
                    case 42:
                        return jLabel264;
                    default:
                        return null;
                }
            case 28:
                switch (jdx) {
                    case 21:
                        return jLabel265;
                    case 22:
                        return jLabel266;
                    case 23:
                        return jLabel267;
                    case 24:
                        return jLabel268;
                    case 25:
                        return jLabel269;
                    case 26:
                        return jLabel270;
                    case 27:
                        return jLabel271;
                    case 28:
                        return jLabel272;
                    case 29:
                        return jLabel273;
                    case 30:
                        return jLabel274;
                    case 31:
                        return jLabel275;
                    case 32:
                        return jLabel276;
                    case 33:
                        return jLabel277;
                    case 34:
                        return jLabel278;
                    case 35:
                        return jLabel279;
                    case 36:
                        return jLabel280;
                    case 37:
                        return jLabel281;
                    case 38:
                        return jLabel282;
                    case 39:
                        return jLabel283;
                    case 40:
                        return jLabel284;
                    case 41:
                        return jLabel285;
                    default:
                        return null;
                }
            case 27:
                switch (jdx) {
                    case 21:
                        return jLabel286;
                    case 22:
                        return jLabel287;
                    case 23:
                        return jLabel288;
                    case 24:
                        return jLabel289;
                    case 25:
                        return jLabel290;
                    case 26:
                        return jLabel291;
                    case 27:
                        return jLabel292;
                    case 28:
                        return jLabel293;
                    case 29:
                        return jLabel294;
                    case 30:
                        return jLabel295;
                    case 31:
                        return jLabel296;
                    case 32:
                        return jLabel297;
                    case 33:
                        return jLabel298;
                    case 34:
                        return jLabel299;
                    case 35:
                        return jLabel300;
                    case 36:
                        return jLabel301;
                    case 37:
                        return jLabel302;
                    case 38:
                        return jLabel303;
                    case 39:
                        return jLabel304;
                    case 40:
                        return jLabel305;
                    case 41:
                        return jLabel306;
                    default:
                        return null;
                }
            case 26:
                switch (jdx) {
                    case 21:
                        return jLabel307;
                    case 22:
                        return jLabel308;
                    case 23:
                        return jLabel309;
                    case 24:
                        return jLabel310;
                    case 25:
                        return jLabel311;
                    case 26:
                        return jLabel312;
                    case 27:
                        return jLabel313;
                    case 28:
                        return jLabel314;
                    case 29:
                        return jLabel315;
                    case 30:
                        return jLabel316;
                    case 31:
                        return jLabel317;
                    case 32:
                        return jLabel318;
                    case 33:
                        return jLabel319;
                    case 34:
                        return jLabel320;
                    case 35:
                        return jLabel321;
                    case 36:
                        return jLabel322;
                    case 37:
                        return jLabel323;
                    case 38:
                        return jLabel324;
                    case 39:
                        return jLabel325;
                    case 40:
                        return jLabel326;
                    case 41:
                        return jLabel327;
                    default:
                        return null;
                }
            case 25:
                switch (jdx) {
                    case 22:
                        return jLabel328;
                    case 23:
                        return jLabel329;
                    case 24:
                        return jLabel330;
                    case 25:
                        return jLabel331;
                    case 26:
                        return jLabel332;
                    case 27:
                        return jLabel333;
                    case 28:
                        return jLabel334;
                    case 29:
                        return jLabel335;
                    case 30:
                        return jLabel336;
                    case 31:
                        return jLabel337;
                    case 32:
                        return jLabel338;
                    case 33:
                        return jLabel339;
                    case 34:
                        return jLabel340;
                    case 35:
                        return jLabel341;
                    case 36:
                        return jLabel342;
                    case 37:
                        return jLabel343;
                    case 38:
                        return jLabel344;
                    case 39:
                        return jLabel345;
                    case 40:
                        return jLabel346;
                    default:
                        return null;
                }
            case 24:
                switch (jdx) {
                    case 22:
                        return jLabel347;
                    case 23:
                        return jLabel348;
                    case 24:
                        return jLabel349;
                    case 25:
                        return jLabel350;
                    case 26:
                        return jLabel351;
                    case 27:
                        return jLabel352;
                    case 28:
                        return jLabel353;
                    case 29:
                        return jLabel354;
                    case 30:
                        return jLabel355;
                    case 31:
                        return jLabel356;
                    case 32:
                        return jLabel357;
                    case 33:
                        return jLabel358;
                    case 34:
                        return jLabel359;
                    case 35:
                        return jLabel360;
                    case 36:
                        return jLabel361;
                    case 37:
                        return jLabel362;
                    case 38:
                        return jLabel363;
                    case 39:
                        return jLabel364;
                    case 40:
                        return jLabel365;
                    default:
                        return null;
                }
            case 23:
                switch (jdx) {
                    case 23:
                        return jLabel366;
                    case 24:
                        return jLabel367;
                    case 25:
                        return jLabel368;
                    case 26:
                        return jLabel369;
                    case 27:
                        return jLabel370;
                    case 28:
                        return jLabel371;
                    case 29:
                        return jLabel372;
                    case 30:
                        return jLabel373;
                    case 31:
                        return jLabel374;
                    case 32:
                        return jLabel375;
                    case 33:
                        return jLabel376;
                    case 34:
                        return jLabel377;
                    case 35:
                        return jLabel378;
                    case 36:
                        return jLabel379;
                    case 37:
                        return jLabel380;
                    case 38:
                        return jLabel381;
                    case 39:
                        return jLabel382;
                    default:
                        return null;
                }
            case 22:
                switch (jdx) {
                    case 24:
                        return jLabel383;
                    case 25:
                        return jLabel384;
                    case 26:
                        return jLabel385;
                    case 27:
                        return jLabel386;
                    case 28:
                        return jLabel387;
                    case 29:
                        return jLabel388;
                    case 30:
                        return jLabel389;
                    case 31:
                        return jLabel390;
                    case 32:
                        return jLabel391;
                    case 33:
                        return jLabel392;
                    case 34:
                        return jLabel393;
                    case 35:
                        return jLabel394;
                    case 36:
                        return jLabel395;
                    case 37:
                        return jLabel396;
                    case 38:
                        return jLabel397;
                    default:
                        return null;
                }
            case 21:
                switch (jdx) {
                    case 26:
                        return jLabel398;
                    case 27:
                        return jLabel399;
                    case 28:
                        return jLabel400;
                    case 29:
                        return jLabel401;
                    case 30:
                        return jLabel402;
                    case 31:
                        return jLabel403;
                    case 32:
                        return jLabel404;
                    case 33:
                        return jLabel405;
                    case 34:
                        return jLabel406;
                    case 35:
                        return jLabel407;
                    case 36:
                        return jLabel408;
                    default:
                        return null;
                }
            case 20:
                switch (jdx) {
                    case 29:
                        return jLabel409;
                    case 30:
                        return jLabel410;
                    case 31:
                        return jLabel411;
                    case 32:
                        return jLabel412;
                    case 33:
                        return jLabel413;
                    default:
                        return null;
                }
            default:
                return null;
        }
        // </editor-fold>
    }

    /**
     * As there are 413 jLabel elements, initializing them all in the GUI
     * builder will result in an error. It is simply too large for one method
     * and a bad practice. However, I want to keep using the GUI builder with
     * previews and WYSIWYG possibilities.
     *
     * <p>
     * error: code too large private void initComponents()
     *
     * <p>
     * Therefore parts of the initialization, which can be organized way easier,
     * were moved to this mehtod.
     */
    private void initComponentsProperties() {
        javax.swing.JLabel jLabel;
        for (int idx = 20; idx < 43; idx++) {
            for (int jdx = 20; jdx < 43; jdx++) {
                jLabel = getLabel(idx, jdx);
                if (jLabel == null) {
                    continue;
                }
                // Fix all sizes variables:
                jLabel.setMaximumSize(new java.awt.Dimension(22, 22));
                jLabel.setMinimumSize(new java.awt.Dimension(22, 22));
                jLabel.setPreferredSize(new java.awt.Dimension(22, 22));
                // Non-transparent background:
                jLabel.setOpaque(true);
                switch (ChannelData.getChannelType(idx, jdx)) {
                    case FUEL:
                        jLabel.setBackground(new java.awt.Color(128, 128, 128));
                        break;
                    case MANUAL_CONTROLROD:
                        jLabel.setBackground(new java.awt.Color(128, 128, 0));
                        break;
                    case AUTOMATIC_CONTROLROD, SHORT_CONTROLROD:
                        jLabel.setBackground(new java.awt.Color(0, 0, 128));
                        break;
                }
            }
        }
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        jLabel7 = new javax.swing.JLabel();
        jLabel8 = new javax.swing.JLabel();
        jLabel9 = new javax.swing.JLabel();
        jLabel10 = new javax.swing.JLabel();
        jLabel11 = new javax.swing.JLabel();
        jLabel12 = new javax.swing.JLabel();
        jLabel13 = new javax.swing.JLabel();
        jLabel14 = new javax.swing.JLabel();
        jLabel15 = new javax.swing.JLabel();
        jLabel16 = new javax.swing.JLabel();
        jLabel17 = new javax.swing.JLabel();
        jLabel18 = new javax.swing.JLabel();
        jLabel19 = new javax.swing.JLabel();
        jLabel20 = new javax.swing.JLabel();
        jLabel21 = new javax.swing.JLabel();
        jLabel22 = new javax.swing.JLabel();
        jLabel23 = new javax.swing.JLabel();
        jLabel24 = new javax.swing.JLabel();
        jLabel25 = new javax.swing.JLabel();
        jLabel26 = new javax.swing.JLabel();
        jLabel27 = new javax.swing.JLabel();
        jLabel28 = new javax.swing.JLabel();
        jLabel29 = new javax.swing.JLabel();
        jLabel30 = new javax.swing.JLabel();
        jLabel31 = new javax.swing.JLabel();
        jLabel32 = new javax.swing.JLabel();
        jLabel33 = new javax.swing.JLabel();
        jLabel34 = new javax.swing.JLabel();
        jLabel35 = new javax.swing.JLabel();
        jLabel36 = new javax.swing.JLabel();
        jLabel37 = new javax.swing.JLabel();
        jLabel38 = new javax.swing.JLabel();
        jLabel39 = new javax.swing.JLabel();
        jLabel40 = new javax.swing.JLabel();
        jLabel41 = new javax.swing.JLabel();
        jLabel42 = new javax.swing.JLabel();
        jLabel43 = new javax.swing.JLabel();
        jLabel44 = new javax.swing.JLabel();
        jLabel45 = new javax.swing.JLabel();
        jLabel46 = new javax.swing.JLabel();
        jLabel47 = new javax.swing.JLabel();
        jLabel48 = new javax.swing.JLabel();
        jLabel49 = new javax.swing.JLabel();
        jLabel50 = new javax.swing.JLabel();
        jLabel51 = new javax.swing.JLabel();
        jLabel52 = new javax.swing.JLabel();
        jLabel53 = new javax.swing.JLabel();
        jLabel54 = new javax.swing.JLabel();
        jLabel55 = new javax.swing.JLabel();
        jLabel56 = new javax.swing.JLabel();
        jLabel57 = new javax.swing.JLabel();
        jLabel58 = new javax.swing.JLabel();
        jLabel59 = new javax.swing.JLabel();
        jLabel60 = new javax.swing.JLabel();
        jLabel61 = new javax.swing.JLabel();
        jLabel62 = new javax.swing.JLabel();
        jLabel63 = new javax.swing.JLabel();
        jLabel64 = new javax.swing.JLabel();
        jLabel65 = new javax.swing.JLabel();
        jLabel66 = new javax.swing.JLabel();
        jLabel67 = new javax.swing.JLabel();
        jLabel68 = new javax.swing.JLabel();
        jLabel69 = new javax.swing.JLabel();
        jLabel70 = new javax.swing.JLabel();
        jLabel71 = new javax.swing.JLabel();
        jLabel72 = new javax.swing.JLabel();
        jLabel73 = new javax.swing.JLabel();
        jLabel74 = new javax.swing.JLabel();
        jLabel75 = new javax.swing.JLabel();
        jLabel76 = new javax.swing.JLabel();
        jLabel77 = new javax.swing.JLabel();
        jLabel78 = new javax.swing.JLabel();
        jLabel79 = new javax.swing.JLabel();
        jLabel80 = new javax.swing.JLabel();
        jLabel81 = new javax.swing.JLabel();
        jLabel82 = new javax.swing.JLabel();
        jLabel83 = new javax.swing.JLabel();
        jLabel84 = new javax.swing.JLabel();
        jLabel85 = new javax.swing.JLabel();
        jLabel86 = new javax.swing.JLabel();
        jLabel87 = new javax.swing.JLabel();
        jLabel88 = new javax.swing.JLabel();
        jLabel89 = new javax.swing.JLabel();
        jLabel90 = new javax.swing.JLabel();
        jLabel91 = new javax.swing.JLabel();
        jLabel92 = new javax.swing.JLabel();
        jLabel93 = new javax.swing.JLabel();
        jLabel94 = new javax.swing.JLabel();
        jLabel95 = new javax.swing.JLabel();
        jLabel96 = new javax.swing.JLabel();
        jLabel97 = new javax.swing.JLabel();
        jLabel98 = new javax.swing.JLabel();
        jLabel99 = new javax.swing.JLabel();
        jLabel100 = new javax.swing.JLabel();
        jLabel101 = new javax.swing.JLabel();
        jLabel102 = new javax.swing.JLabel();
        jLabel103 = new javax.swing.JLabel();
        jLabel104 = new javax.swing.JLabel();
        jLabel105 = new javax.swing.JLabel();
        jLabel106 = new javax.swing.JLabel();
        jLabel107 = new javax.swing.JLabel();
        jLabel108 = new javax.swing.JLabel();
        jLabel109 = new javax.swing.JLabel();
        jLabel110 = new javax.swing.JLabel();
        jLabel111 = new javax.swing.JLabel();
        jLabel112 = new javax.swing.JLabel();
        jLabel113 = new javax.swing.JLabel();
        jLabel114 = new javax.swing.JLabel();
        jLabel115 = new javax.swing.JLabel();
        jLabel116 = new javax.swing.JLabel();
        jLabel117 = new javax.swing.JLabel();
        jLabel118 = new javax.swing.JLabel();
        jLabel119 = new javax.swing.JLabel();
        jLabel120 = new javax.swing.JLabel();
        jLabel121 = new javax.swing.JLabel();
        jLabel122 = new javax.swing.JLabel();
        jLabel123 = new javax.swing.JLabel();
        jLabel124 = new javax.swing.JLabel();
        jLabel125 = new javax.swing.JLabel();
        jLabel126 = new javax.swing.JLabel();
        jLabel127 = new javax.swing.JLabel();
        jLabel128 = new javax.swing.JLabel();
        jLabel129 = new javax.swing.JLabel();
        jLabel130 = new javax.swing.JLabel();
        jLabel131 = new javax.swing.JLabel();
        jLabel132 = new javax.swing.JLabel();
        jLabel133 = new javax.swing.JLabel();
        jLabel134 = new javax.swing.JLabel();
        jLabel135 = new javax.swing.JLabel();
        jLabel136 = new javax.swing.JLabel();
        jLabel137 = new javax.swing.JLabel();
        jLabel138 = new javax.swing.JLabel();
        jLabel139 = new javax.swing.JLabel();
        jLabel140 = new javax.swing.JLabel();
        jLabel141 = new javax.swing.JLabel();
        jLabel142 = new javax.swing.JLabel();
        jLabel143 = new javax.swing.JLabel();
        jLabel144 = new javax.swing.JLabel();
        jLabel145 = new javax.swing.JLabel();
        jLabel146 = new javax.swing.JLabel();
        jLabel147 = new javax.swing.JLabel();
        jLabel148 = new javax.swing.JLabel();
        jLabel149 = new javax.swing.JLabel();
        jLabel150 = new javax.swing.JLabel();
        jLabel151 = new javax.swing.JLabel();
        jLabel152 = new javax.swing.JLabel();
        jLabel153 = new javax.swing.JLabel();
        jLabel154 = new javax.swing.JLabel();
        jLabel155 = new javax.swing.JLabel();
        jLabel156 = new javax.swing.JLabel();
        jLabel157 = new javax.swing.JLabel();
        jLabel158 = new javax.swing.JLabel();
        jLabel159 = new javax.swing.JLabel();
        jLabel160 = new javax.swing.JLabel();
        jLabel161 = new javax.swing.JLabel();
        jLabel162 = new javax.swing.JLabel();
        jLabel163 = new javax.swing.JLabel();
        jLabel164 = new javax.swing.JLabel();
        jLabel165 = new javax.swing.JLabel();
        jLabel166 = new javax.swing.JLabel();
        jLabel167 = new javax.swing.JLabel();
        jLabel168 = new javax.swing.JLabel();
        jLabel169 = new javax.swing.JLabel();
        jLabel170 = new javax.swing.JLabel();
        jLabel171 = new javax.swing.JLabel();
        jLabel172 = new javax.swing.JLabel();
        jLabel173 = new javax.swing.JLabel();
        jLabel174 = new javax.swing.JLabel();
        jLabel175 = new javax.swing.JLabel();
        jLabel176 = new javax.swing.JLabel();
        jLabel177 = new javax.swing.JLabel();
        jLabel178 = new javax.swing.JLabel();
        jLabel179 = new javax.swing.JLabel();
        jLabel180 = new javax.swing.JLabel();
        jLabel181 = new javax.swing.JLabel();
        jLabel182 = new javax.swing.JLabel();
        jLabel183 = new javax.swing.JLabel();
        jLabel184 = new javax.swing.JLabel();
        jLabel185 = new javax.swing.JLabel();
        jLabel186 = new javax.swing.JLabel();
        jLabel187 = new javax.swing.JLabel();
        jLabel188 = new javax.swing.JLabel();
        jLabel189 = new javax.swing.JLabel();
        jLabel190 = new javax.swing.JLabel();
        jLabel191 = new javax.swing.JLabel();
        jLabel192 = new javax.swing.JLabel();
        jLabel193 = new javax.swing.JLabel();
        jLabel194 = new javax.swing.JLabel();
        jLabel195 = new javax.swing.JLabel();
        jLabel196 = new javax.swing.JLabel();
        jLabel197 = new javax.swing.JLabel();
        jLabel198 = new javax.swing.JLabel();
        jLabel199 = new javax.swing.JLabel();
        jLabel200 = new javax.swing.JLabel();
        jLabel201 = new javax.swing.JLabel();
        jLabel202 = new javax.swing.JLabel();
        jLabel203 = new javax.swing.JLabel();
        jLabel204 = new javax.swing.JLabel();
        jLabel205 = new javax.swing.JLabel();
        jLabel206 = new javax.swing.JLabel();
        jLabel207 = new javax.swing.JLabel();
        jLabel208 = new javax.swing.JLabel();
        jLabel209 = new javax.swing.JLabel();
        jLabel210 = new javax.swing.JLabel();
        jLabel211 = new javax.swing.JLabel();
        jLabel212 = new javax.swing.JLabel();
        jLabel213 = new javax.swing.JLabel();
        jLabel214 = new javax.swing.JLabel();
        jLabel215 = new javax.swing.JLabel();
        jLabel216 = new javax.swing.JLabel();
        jLabel217 = new javax.swing.JLabel();
        jLabel218 = new javax.swing.JLabel();
        jLabel219 = new javax.swing.JLabel();
        jLabel220 = new javax.swing.JLabel();
        jLabel221 = new javax.swing.JLabel();
        jLabel222 = new javax.swing.JLabel();
        jLabel223 = new javax.swing.JLabel();
        jLabel224 = new javax.swing.JLabel();
        jLabel225 = new javax.swing.JLabel();
        jLabel226 = new javax.swing.JLabel();
        jLabel227 = new javax.swing.JLabel();
        jLabel228 = new javax.swing.JLabel();
        jLabel229 = new javax.swing.JLabel();
        jLabel230 = new javax.swing.JLabel();
        jLabel231 = new javax.swing.JLabel();
        jLabel232 = new javax.swing.JLabel();
        jLabel233 = new javax.swing.JLabel();
        jLabel234 = new javax.swing.JLabel();
        jLabel235 = new javax.swing.JLabel();
        jLabel236 = new javax.swing.JLabel();
        jLabel237 = new javax.swing.JLabel();
        jLabel238 = new javax.swing.JLabel();
        jLabel239 = new javax.swing.JLabel();
        jLabel240 = new javax.swing.JLabel();
        jLabel241 = new javax.swing.JLabel();
        jLabel242 = new javax.swing.JLabel();
        jLabel243 = new javax.swing.JLabel();
        jLabel244 = new javax.swing.JLabel();
        jLabel245 = new javax.swing.JLabel();
        jLabel246 = new javax.swing.JLabel();
        jLabel247 = new javax.swing.JLabel();
        jLabel248 = new javax.swing.JLabel();
        jLabel249 = new javax.swing.JLabel();
        jLabel250 = new javax.swing.JLabel();
        jLabel251 = new javax.swing.JLabel();
        jLabel252 = new javax.swing.JLabel();
        jLabel253 = new javax.swing.JLabel();
        jLabel254 = new javax.swing.JLabel();
        jLabel255 = new javax.swing.JLabel();
        jLabel256 = new javax.swing.JLabel();
        jLabel257 = new javax.swing.JLabel();
        jLabel258 = new javax.swing.JLabel();
        jLabel259 = new javax.swing.JLabel();
        jLabel260 = new javax.swing.JLabel();
        jLabel261 = new javax.swing.JLabel();
        jLabel262 = new javax.swing.JLabel();
        jLabel263 = new javax.swing.JLabel();
        jLabel264 = new javax.swing.JLabel();
        jLabel265 = new javax.swing.JLabel();
        jLabel266 = new javax.swing.JLabel();
        jLabel267 = new javax.swing.JLabel();
        jLabel268 = new javax.swing.JLabel();
        jLabel269 = new javax.swing.JLabel();
        jLabel270 = new javax.swing.JLabel();
        jLabel271 = new javax.swing.JLabel();
        jLabel272 = new javax.swing.JLabel();
        jLabel273 = new javax.swing.JLabel();
        jLabel274 = new javax.swing.JLabel();
        jLabel275 = new javax.swing.JLabel();
        jLabel276 = new javax.swing.JLabel();
        jLabel277 = new javax.swing.JLabel();
        jLabel278 = new javax.swing.JLabel();
        jLabel279 = new javax.swing.JLabel();
        jLabel280 = new javax.swing.JLabel();
        jLabel281 = new javax.swing.JLabel();
        jLabel282 = new javax.swing.JLabel();
        jLabel283 = new javax.swing.JLabel();
        jLabel284 = new javax.swing.JLabel();
        jLabel285 = new javax.swing.JLabel();
        jLabel286 = new javax.swing.JLabel();
        jLabel287 = new javax.swing.JLabel();
        jLabel288 = new javax.swing.JLabel();
        jLabel289 = new javax.swing.JLabel();
        jLabel290 = new javax.swing.JLabel();
        jLabel291 = new javax.swing.JLabel();
        jLabel292 = new javax.swing.JLabel();
        jLabel293 = new javax.swing.JLabel();
        jLabel294 = new javax.swing.JLabel();
        jLabel295 = new javax.swing.JLabel();
        jLabel296 = new javax.swing.JLabel();
        jLabel297 = new javax.swing.JLabel();
        jLabel298 = new javax.swing.JLabel();
        jLabel299 = new javax.swing.JLabel();
        jLabel300 = new javax.swing.JLabel();
        jLabel301 = new javax.swing.JLabel();
        jLabel302 = new javax.swing.JLabel();
        jLabel303 = new javax.swing.JLabel();
        jLabel304 = new javax.swing.JLabel();
        jLabel305 = new javax.swing.JLabel();
        jLabel306 = new javax.swing.JLabel();
        jLabel307 = new javax.swing.JLabel();
        jLabel308 = new javax.swing.JLabel();
        jLabel309 = new javax.swing.JLabel();
        jLabel310 = new javax.swing.JLabel();
        jLabel311 = new javax.swing.JLabel();
        jLabel312 = new javax.swing.JLabel();
        jLabel313 = new javax.swing.JLabel();
        jLabel314 = new javax.swing.JLabel();
        jLabel315 = new javax.swing.JLabel();
        jLabel316 = new javax.swing.JLabel();
        jLabel317 = new javax.swing.JLabel();
        jLabel318 = new javax.swing.JLabel();
        jLabel319 = new javax.swing.JLabel();
        jLabel320 = new javax.swing.JLabel();
        jLabel321 = new javax.swing.JLabel();
        jLabel322 = new javax.swing.JLabel();
        jLabel323 = new javax.swing.JLabel();
        jLabel324 = new javax.swing.JLabel();
        jLabel325 = new javax.swing.JLabel();
        jLabel326 = new javax.swing.JLabel();
        jLabel327 = new javax.swing.JLabel();
        jLabel328 = new javax.swing.JLabel();
        jLabel329 = new javax.swing.JLabel();
        jLabel330 = new javax.swing.JLabel();
        jLabel331 = new javax.swing.JLabel();
        jLabel332 = new javax.swing.JLabel();
        jLabel333 = new javax.swing.JLabel();
        jLabel334 = new javax.swing.JLabel();
        jLabel335 = new javax.swing.JLabel();
        jLabel336 = new javax.swing.JLabel();
        jLabel337 = new javax.swing.JLabel();
        jLabel338 = new javax.swing.JLabel();
        jLabel339 = new javax.swing.JLabel();
        jLabel340 = new javax.swing.JLabel();
        jLabel341 = new javax.swing.JLabel();
        jLabel342 = new javax.swing.JLabel();
        jLabel343 = new javax.swing.JLabel();
        jLabel344 = new javax.swing.JLabel();
        jLabel345 = new javax.swing.JLabel();
        jLabel346 = new javax.swing.JLabel();
        jLabel347 = new javax.swing.JLabel();
        jLabel348 = new javax.swing.JLabel();
        jLabel349 = new javax.swing.JLabel();
        jLabel350 = new javax.swing.JLabel();
        jLabel351 = new javax.swing.JLabel();
        jLabel352 = new javax.swing.JLabel();
        jLabel353 = new javax.swing.JLabel();
        jLabel354 = new javax.swing.JLabel();
        jLabel355 = new javax.swing.JLabel();
        jLabel356 = new javax.swing.JLabel();
        jLabel357 = new javax.swing.JLabel();
        jLabel358 = new javax.swing.JLabel();
        jLabel359 = new javax.swing.JLabel();
        jLabel360 = new javax.swing.JLabel();
        jLabel361 = new javax.swing.JLabel();
        jLabel362 = new javax.swing.JLabel();
        jLabel363 = new javax.swing.JLabel();
        jLabel364 = new javax.swing.JLabel();
        jLabel365 = new javax.swing.JLabel();
        jLabel366 = new javax.swing.JLabel();
        jLabel367 = new javax.swing.JLabel();
        jLabel368 = new javax.swing.JLabel();
        jLabel369 = new javax.swing.JLabel();
        jLabel370 = new javax.swing.JLabel();
        jLabel371 = new javax.swing.JLabel();
        jLabel372 = new javax.swing.JLabel();
        jLabel373 = new javax.swing.JLabel();
        jLabel374 = new javax.swing.JLabel();
        jLabel375 = new javax.swing.JLabel();
        jLabel376 = new javax.swing.JLabel();
        jLabel377 = new javax.swing.JLabel();
        jLabel378 = new javax.swing.JLabel();
        jLabel379 = new javax.swing.JLabel();
        jLabel380 = new javax.swing.JLabel();
        jLabel381 = new javax.swing.JLabel();
        jLabel382 = new javax.swing.JLabel();
        jLabel383 = new javax.swing.JLabel();
        jLabel384 = new javax.swing.JLabel();
        jLabel385 = new javax.swing.JLabel();
        jLabel386 = new javax.swing.JLabel();
        jLabel387 = new javax.swing.JLabel();
        jLabel388 = new javax.swing.JLabel();
        jLabel389 = new javax.swing.JLabel();
        jLabel390 = new javax.swing.JLabel();
        jLabel391 = new javax.swing.JLabel();
        jLabel392 = new javax.swing.JLabel();
        jLabel393 = new javax.swing.JLabel();
        jLabel394 = new javax.swing.JLabel();
        jLabel395 = new javax.swing.JLabel();
        jLabel396 = new javax.swing.JLabel();
        jLabel397 = new javax.swing.JLabel();
        jLabel398 = new javax.swing.JLabel();
        jLabel399 = new javax.swing.JLabel();
        jLabel400 = new javax.swing.JLabel();
        jLabel401 = new javax.swing.JLabel();
        jLabel402 = new javax.swing.JLabel();
        jLabel403 = new javax.swing.JLabel();
        jLabel404 = new javax.swing.JLabel();
        jLabel405 = new javax.swing.JLabel();
        jLabel406 = new javax.swing.JLabel();
        jLabel407 = new javax.swing.JLabel();
        jLabel408 = new javax.swing.JLabel();
        jLabel409 = new javax.swing.JLabel();
        jLabel410 = new javax.swing.JLabel();
        jLabel411 = new javax.swing.JLabel();
        jLabel412 = new javax.swing.JLabel();
        jLabel413 = new javax.swing.JLabel();

        setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jLabel1.setFont(jLabel1.getFont().deriveFont(jLabel1.getFont().getSize()-5f));
        jLabel1.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel1.setText("42-29");
        jLabel1.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        add(jLabel1, new org.netbeans.lib.awtextra.AbsoluteConstraints(207, 0, 22, 22));

        jLabel2.setFont(jLabel2.getFont().deriveFont(jLabel2.getFont().getSize()-5f));
        jLabel2.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel2.setText("42-30");
        jLabel2.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        add(jLabel2, new org.netbeans.lib.awtextra.AbsoluteConstraints(230, 0, 22, 22));

        jLabel3.setFont(jLabel3.getFont().deriveFont(jLabel3.getFont().getSize()-5f));
        jLabel3.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel3.setText("42-31");
        jLabel3.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        add(jLabel3, new org.netbeans.lib.awtextra.AbsoluteConstraints(253, 0, 22, 22));

        jLabel4.setFont(jLabel4.getFont().deriveFont(jLabel4.getFont().getSize()-5f));
        jLabel4.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel4.setText("42-32");
        jLabel4.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        add(jLabel4, new org.netbeans.lib.awtextra.AbsoluteConstraints(276, 0, 22, 22));

        jLabel5.setFont(jLabel5.getFont().deriveFont(jLabel5.getFont().getSize()-5f));
        jLabel5.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel5.setText("42-33");
        jLabel5.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        add(jLabel5, new org.netbeans.lib.awtextra.AbsoluteConstraints(299, 0, 22, 22));

        jLabel6.setFont(jLabel6.getFont().deriveFont(jLabel6.getFont().getSize()-5f));
        jLabel6.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel6.setText("41-26");
        jLabel6.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        add(jLabel6, new org.netbeans.lib.awtextra.AbsoluteConstraints(138, 23, 22, 22));

        jLabel7.setFont(jLabel7.getFont().deriveFont(jLabel7.getFont().getSize()-5f));
        jLabel7.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel7.setText("41-27");
        jLabel7.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        add(jLabel7, new org.netbeans.lib.awtextra.AbsoluteConstraints(161, 23, 22, 22));

        jLabel8.setFont(jLabel8.getFont().deriveFont(jLabel8.getFont().getSize()-5f));
        jLabel8.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel8.setText("41-28");
        jLabel8.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        add(jLabel8, new org.netbeans.lib.awtextra.AbsoluteConstraints(184, 23, 22, 22));

        jLabel9.setFont(jLabel9.getFont().deriveFont(jLabel9.getFont().getSize()-5f));
        jLabel9.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel9.setText("41-29");
        jLabel9.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        add(jLabel9, new org.netbeans.lib.awtextra.AbsoluteConstraints(207, 23, 22, 22));

        jLabel10.setFont(jLabel10.getFont().deriveFont(jLabel10.getFont().getSize()-5f));
        jLabel10.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel10.setText("41-30");
        jLabel10.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        add(jLabel10, new org.netbeans.lib.awtextra.AbsoluteConstraints(230, 23, 22, 22));

        jLabel11.setFont(jLabel11.getFont().deriveFont(jLabel11.getFont().getSize()-5f));
        jLabel11.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel11.setText("41-31");
        jLabel11.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        add(jLabel11, new org.netbeans.lib.awtextra.AbsoluteConstraints(253, 23, 22, 22));

        jLabel12.setFont(jLabel12.getFont().deriveFont(jLabel12.getFont().getSize()-5f));
        jLabel12.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel12.setText("41-32");
        jLabel12.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        add(jLabel12, new org.netbeans.lib.awtextra.AbsoluteConstraints(276, 23, 22, 22));

        jLabel13.setFont(jLabel13.getFont().deriveFont(jLabel13.getFont().getSize()-5f));
        jLabel13.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel13.setText("41-33");
        jLabel13.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        add(jLabel13, new org.netbeans.lib.awtextra.AbsoluteConstraints(299, 23, 22, 22));

        jLabel14.setFont(jLabel14.getFont().deriveFont(jLabel14.getFont().getSize()-5f));
        jLabel14.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel14.setText("41-34");
        jLabel14.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        add(jLabel14, new org.netbeans.lib.awtextra.AbsoluteConstraints(322, 23, 22, 22));

        jLabel15.setFont(jLabel15.getFont().deriveFont(jLabel15.getFont().getSize()-5f));
        jLabel15.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel15.setText("41-35");
        jLabel15.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        add(jLabel15, new org.netbeans.lib.awtextra.AbsoluteConstraints(345, 23, 22, 22));

        jLabel16.setFont(jLabel16.getFont().deriveFont(jLabel16.getFont().getSize()-5f));
        jLabel16.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel16.setText("41-36");
        jLabel16.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        add(jLabel16, new org.netbeans.lib.awtextra.AbsoluteConstraints(368, 23, 22, 22));

        jLabel17.setFont(jLabel17.getFont().deriveFont(jLabel17.getFont().getSize()-5f));
        jLabel17.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel17.setText("40-24");
        jLabel17.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        add(jLabel17, new org.netbeans.lib.awtextra.AbsoluteConstraints(92, 46, 22, 22));

        jLabel18.setFont(jLabel18.getFont().deriveFont(jLabel18.getFont().getSize()-5f));
        jLabel18.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel18.setText("40-25");
        jLabel18.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        add(jLabel18, new org.netbeans.lib.awtextra.AbsoluteConstraints(115, 46, 22, 22));

        jLabel19.setFont(jLabel19.getFont().deriveFont(jLabel19.getFont().getSize()-5f));
        jLabel19.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel19.setText("40-26");
        jLabel19.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        add(jLabel19, new org.netbeans.lib.awtextra.AbsoluteConstraints(138, 46, 22, 22));

        jLabel20.setFont(jLabel20.getFont().deriveFont(jLabel20.getFont().getSize()-5f));
        jLabel20.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel20.setText("40-27");
        jLabel20.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        add(jLabel20, new org.netbeans.lib.awtextra.AbsoluteConstraints(161, 46, 22, 22));

        jLabel21.setFont(jLabel21.getFont().deriveFont(jLabel21.getFont().getSize()-5f));
        jLabel21.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel21.setText("40-28");
        jLabel21.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        add(jLabel21, new org.netbeans.lib.awtextra.AbsoluteConstraints(184, 46, 22, 22));

        jLabel22.setFont(jLabel22.getFont().deriveFont(jLabel22.getFont().getSize()-5f));
        jLabel22.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel22.setText("40-29");
        jLabel22.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        add(jLabel22, new org.netbeans.lib.awtextra.AbsoluteConstraints(207, 46, 22, 22));

        jLabel23.setFont(jLabel23.getFont().deriveFont(jLabel23.getFont().getSize()-5f));
        jLabel23.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel23.setText("40-30");
        jLabel23.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        add(jLabel23, new org.netbeans.lib.awtextra.AbsoluteConstraints(230, 46, 22, 22));

        jLabel24.setFont(jLabel24.getFont().deriveFont(jLabel24.getFont().getSize()-5f));
        jLabel24.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel24.setText("40-31");
        jLabel24.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        add(jLabel24, new org.netbeans.lib.awtextra.AbsoluteConstraints(253, 46, 22, 22));

        jLabel25.setFont(jLabel25.getFont().deriveFont(jLabel25.getFont().getSize()-5f));
        jLabel25.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel25.setText("40-32");
        jLabel25.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        add(jLabel25, new org.netbeans.lib.awtextra.AbsoluteConstraints(276, 46, 22, 22));

        jLabel26.setFont(jLabel26.getFont().deriveFont(jLabel26.getFont().getSize()-5f));
        jLabel26.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel26.setText("40-33");
        jLabel26.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        add(jLabel26, new org.netbeans.lib.awtextra.AbsoluteConstraints(299, 46, 22, 22));

        jLabel27.setFont(jLabel27.getFont().deriveFont(jLabel27.getFont().getSize()-5f));
        jLabel27.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel27.setText("40-34");
        jLabel27.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        add(jLabel27, new org.netbeans.lib.awtextra.AbsoluteConstraints(322, 46, 22, 22));

        jLabel28.setFont(jLabel28.getFont().deriveFont(jLabel28.getFont().getSize()-5f));
        jLabel28.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel28.setText("40-35");
        jLabel28.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        add(jLabel28, new org.netbeans.lib.awtextra.AbsoluteConstraints(345, 46, 22, 22));

        jLabel29.setFont(jLabel29.getFont().deriveFont(jLabel29.getFont().getSize()-5f));
        jLabel29.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel29.setText("40-36");
        jLabel29.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        add(jLabel29, new org.netbeans.lib.awtextra.AbsoluteConstraints(368, 46, 22, 22));

        jLabel30.setFont(jLabel30.getFont().deriveFont(jLabel30.getFont().getSize()-5f));
        jLabel30.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel30.setText("40-37");
        jLabel30.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        add(jLabel30, new org.netbeans.lib.awtextra.AbsoluteConstraints(391, 46, 22, 22));

        jLabel31.setFont(jLabel31.getFont().deriveFont(jLabel31.getFont().getSize()-5f));
        jLabel31.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel31.setText("40-38");
        jLabel31.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        add(jLabel31, new org.netbeans.lib.awtextra.AbsoluteConstraints(414, 46, 22, 22));

        jLabel32.setFont(jLabel32.getFont().deriveFont(jLabel32.getFont().getSize()-5f));
        jLabel32.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel32.setText("39-23");
        jLabel32.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        add(jLabel32, new org.netbeans.lib.awtextra.AbsoluteConstraints(69, 69, 22, 22));

        jLabel33.setFont(jLabel33.getFont().deriveFont(jLabel33.getFont().getSize()-5f));
        jLabel33.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel33.setText("39-24");
        jLabel33.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        add(jLabel33, new org.netbeans.lib.awtextra.AbsoluteConstraints(92, 69, 22, 22));

        jLabel34.setFont(jLabel34.getFont().deriveFont(jLabel34.getFont().getSize()-5f));
        jLabel34.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel34.setText("39-25");
        jLabel34.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        add(jLabel34, new org.netbeans.lib.awtextra.AbsoluteConstraints(115, 69, 22, 22));

        jLabel35.setFont(jLabel35.getFont().deriveFont(jLabel35.getFont().getSize()-5f));
        jLabel35.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel35.setText("39-26");
        jLabel35.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        add(jLabel35, new org.netbeans.lib.awtextra.AbsoluteConstraints(138, 69, 22, 22));

        jLabel36.setFont(jLabel36.getFont().deriveFont(jLabel36.getFont().getSize()-5f));
        jLabel36.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel36.setText("39-27");
        jLabel36.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        add(jLabel36, new org.netbeans.lib.awtextra.AbsoluteConstraints(161, 69, 22, 22));

        jLabel37.setFont(jLabel37.getFont().deriveFont(jLabel37.getFont().getSize()-5f));
        jLabel37.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel37.setText("39-28");
        jLabel37.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        add(jLabel37, new org.netbeans.lib.awtextra.AbsoluteConstraints(184, 69, 22, 22));

        jLabel38.setFont(jLabel38.getFont().deriveFont(jLabel38.getFont().getSize()-5f));
        jLabel38.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel38.setText("39-29");
        jLabel38.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        add(jLabel38, new org.netbeans.lib.awtextra.AbsoluteConstraints(207, 69, 22, 22));

        jLabel39.setFont(jLabel39.getFont().deriveFont(jLabel39.getFont().getSize()-5f));
        jLabel39.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel39.setText("39-30");
        jLabel39.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        add(jLabel39, new org.netbeans.lib.awtextra.AbsoluteConstraints(230, 69, 22, 22));

        jLabel40.setFont(jLabel40.getFont().deriveFont(jLabel40.getFont().getSize()-5f));
        jLabel40.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel40.setText("39-31");
        jLabel40.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        add(jLabel40, new org.netbeans.lib.awtextra.AbsoluteConstraints(253, 69, 22, 22));

        jLabel41.setFont(jLabel41.getFont().deriveFont(jLabel41.getFont().getSize()-5f));
        jLabel41.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel41.setText("39-32");
        jLabel41.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        add(jLabel41, new org.netbeans.lib.awtextra.AbsoluteConstraints(276, 69, 22, 22));

        jLabel42.setFont(jLabel42.getFont().deriveFont(jLabel42.getFont().getSize()-5f));
        jLabel42.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel42.setText("39-33");
        jLabel42.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        add(jLabel42, new org.netbeans.lib.awtextra.AbsoluteConstraints(299, 69, 22, 22));

        jLabel43.setFont(jLabel43.getFont().deriveFont(jLabel43.getFont().getSize()-5f));
        jLabel43.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel43.setText("39-34");
        jLabel43.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        add(jLabel43, new org.netbeans.lib.awtextra.AbsoluteConstraints(322, 69, 22, 22));

        jLabel44.setFont(jLabel44.getFont().deriveFont(jLabel44.getFont().getSize()-5f));
        jLabel44.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel44.setText("39-35");
        jLabel44.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        add(jLabel44, new org.netbeans.lib.awtextra.AbsoluteConstraints(345, 69, 22, 22));

        jLabel45.setFont(jLabel45.getFont().deriveFont(jLabel45.getFont().getSize()-5f));
        jLabel45.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel45.setText("39-36");
        jLabel45.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        add(jLabel45, new org.netbeans.lib.awtextra.AbsoluteConstraints(368, 69, 22, 22));

        jLabel46.setFont(jLabel46.getFont().deriveFont(jLabel46.getFont().getSize()-5f));
        jLabel46.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel46.setText("39-37");
        jLabel46.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        add(jLabel46, new org.netbeans.lib.awtextra.AbsoluteConstraints(391, 69, 22, 22));

        jLabel47.setFont(jLabel47.getFont().deriveFont(jLabel47.getFont().getSize()-5f));
        jLabel47.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel47.setText("39-38");
        jLabel47.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        add(jLabel47, new org.netbeans.lib.awtextra.AbsoluteConstraints(414, 69, 22, 22));

        jLabel48.setFont(jLabel48.getFont().deriveFont(jLabel48.getFont().getSize()-5f));
        jLabel48.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel48.setText("39-39");
        jLabel48.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        add(jLabel48, new org.netbeans.lib.awtextra.AbsoluteConstraints(437, 69, 22, 22));

        jLabel49.setFont(jLabel49.getFont().deriveFont(jLabel49.getFont().getSize()-5f));
        jLabel49.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel49.setText("38-22");
        jLabel49.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        add(jLabel49, new org.netbeans.lib.awtextra.AbsoluteConstraints(46, 92, 22, 22));

        jLabel50.setFont(jLabel50.getFont().deriveFont(jLabel50.getFont().getSize()-5f));
        jLabel50.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel50.setText("38-23");
        jLabel50.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        add(jLabel50, new org.netbeans.lib.awtextra.AbsoluteConstraints(69, 92, 22, 22));

        jLabel51.setFont(jLabel51.getFont().deriveFont(jLabel51.getFont().getSize()-5f));
        jLabel51.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel51.setText("38-24");
        jLabel51.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        add(jLabel51, new org.netbeans.lib.awtextra.AbsoluteConstraints(92, 92, 22, 22));

        jLabel52.setFont(jLabel52.getFont().deriveFont(jLabel52.getFont().getSize()-5f));
        jLabel52.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel52.setText("38-25");
        jLabel52.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        add(jLabel52, new org.netbeans.lib.awtextra.AbsoluteConstraints(115, 92, 22, 22));

        jLabel53.setFont(jLabel53.getFont().deriveFont(jLabel53.getFont().getSize()-5f));
        jLabel53.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel53.setText("38-26");
        jLabel53.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        add(jLabel53, new org.netbeans.lib.awtextra.AbsoluteConstraints(138, 92, 22, 22));

        jLabel54.setFont(jLabel54.getFont().deriveFont(jLabel54.getFont().getSize()-5f));
        jLabel54.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel54.setText("38-27");
        jLabel54.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        add(jLabel54, new org.netbeans.lib.awtextra.AbsoluteConstraints(161, 92, 22, 22));

        jLabel55.setFont(jLabel55.getFont().deriveFont(jLabel55.getFont().getSize()-5f));
        jLabel55.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel55.setText("38-28");
        jLabel55.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        add(jLabel55, new org.netbeans.lib.awtextra.AbsoluteConstraints(184, 92, 22, 22));

        jLabel56.setFont(jLabel56.getFont().deriveFont(jLabel56.getFont().getSize()-5f));
        jLabel56.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel56.setText("38-29");
        jLabel56.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        add(jLabel56, new org.netbeans.lib.awtextra.AbsoluteConstraints(207, 92, 22, 22));

        jLabel57.setFont(jLabel57.getFont().deriveFont(jLabel57.getFont().getSize()-5f));
        jLabel57.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel57.setText("38-30");
        jLabel57.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        add(jLabel57, new org.netbeans.lib.awtextra.AbsoluteConstraints(230, 92, 22, 22));

        jLabel58.setFont(jLabel58.getFont().deriveFont(jLabel58.getFont().getSize()-5f));
        jLabel58.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel58.setText("38-31");
        jLabel58.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        add(jLabel58, new org.netbeans.lib.awtextra.AbsoluteConstraints(253, 92, 22, 22));

        jLabel59.setFont(jLabel59.getFont().deriveFont(jLabel59.getFont().getSize()-5f));
        jLabel59.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel59.setText("38-32");
        jLabel59.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        add(jLabel59, new org.netbeans.lib.awtextra.AbsoluteConstraints(276, 92, 22, 22));

        jLabel60.setFont(jLabel60.getFont().deriveFont(jLabel60.getFont().getSize()-5f));
        jLabel60.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel60.setText("38-33");
        jLabel60.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        add(jLabel60, new org.netbeans.lib.awtextra.AbsoluteConstraints(299, 92, 22, 22));

        jLabel61.setFont(jLabel61.getFont().deriveFont(jLabel61.getFont().getSize()-5f));
        jLabel61.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel61.setText("38-34");
        jLabel61.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        add(jLabel61, new org.netbeans.lib.awtextra.AbsoluteConstraints(322, 92, 22, 22));

        jLabel62.setFont(jLabel62.getFont().deriveFont(jLabel62.getFont().getSize()-5f));
        jLabel62.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel62.setText("38-35");
        jLabel62.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        add(jLabel62, new org.netbeans.lib.awtextra.AbsoluteConstraints(345, 92, 22, 22));

        jLabel63.setFont(jLabel63.getFont().deriveFont(jLabel63.getFont().getSize()-5f));
        jLabel63.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel63.setText("38-36");
        jLabel63.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        add(jLabel63, new org.netbeans.lib.awtextra.AbsoluteConstraints(368, 92, 22, 22));

        jLabel64.setFont(jLabel64.getFont().deriveFont(jLabel64.getFont().getSize()-5f));
        jLabel64.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel64.setText("38-37");
        jLabel64.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        add(jLabel64, new org.netbeans.lib.awtextra.AbsoluteConstraints(391, 92, 22, 22));

        jLabel65.setFont(jLabel65.getFont().deriveFont(jLabel65.getFont().getSize()-5f));
        jLabel65.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel65.setText("38-38");
        jLabel65.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        add(jLabel65, new org.netbeans.lib.awtextra.AbsoluteConstraints(414, 92, 22, 22));

        jLabel66.setFont(jLabel66.getFont().deriveFont(jLabel66.getFont().getSize()-5f));
        jLabel66.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel66.setText("38-39");
        jLabel66.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        add(jLabel66, new org.netbeans.lib.awtextra.AbsoluteConstraints(437, 92, 22, 22));

        jLabel67.setFont(jLabel67.getFont().deriveFont(jLabel67.getFont().getSize()-5f));
        jLabel67.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel67.setText("38-40");
        jLabel67.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        add(jLabel67, new org.netbeans.lib.awtextra.AbsoluteConstraints(460, 92, 22, 22));

        jLabel68.setFont(jLabel68.getFont().deriveFont(jLabel68.getFont().getSize()-5f));
        jLabel68.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel68.setText("37-22");
        jLabel68.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        add(jLabel68, new org.netbeans.lib.awtextra.AbsoluteConstraints(46, 115, 22, 22));

        jLabel69.setFont(jLabel69.getFont().deriveFont(jLabel69.getFont().getSize()-5f));
        jLabel69.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel69.setText("37-23");
        jLabel69.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        add(jLabel69, new org.netbeans.lib.awtextra.AbsoluteConstraints(69, 115, 22, 22));

        jLabel70.setFont(jLabel70.getFont().deriveFont(jLabel70.getFont().getSize()-5f));
        jLabel70.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel70.setText("37-24");
        jLabel70.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        add(jLabel70, new org.netbeans.lib.awtextra.AbsoluteConstraints(92, 115, 22, 22));

        jLabel71.setFont(jLabel71.getFont().deriveFont(jLabel71.getFont().getSize()-5f));
        jLabel71.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel71.setText("37-25");
        jLabel71.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        add(jLabel71, new org.netbeans.lib.awtextra.AbsoluteConstraints(115, 115, 22, 22));

        jLabel72.setFont(jLabel72.getFont().deriveFont(jLabel72.getFont().getSize()-5f));
        jLabel72.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel72.setText("37-26");
        jLabel72.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        add(jLabel72, new org.netbeans.lib.awtextra.AbsoluteConstraints(138, 115, 22, 22));

        jLabel73.setFont(jLabel73.getFont().deriveFont(jLabel73.getFont().getSize()-5f));
        jLabel73.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel73.setText("37-27");
        jLabel73.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        add(jLabel73, new org.netbeans.lib.awtextra.AbsoluteConstraints(161, 115, 22, 22));

        jLabel74.setFont(jLabel74.getFont().deriveFont(jLabel74.getFont().getSize()-5f));
        jLabel74.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel74.setText("37-28");
        jLabel74.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        add(jLabel74, new org.netbeans.lib.awtextra.AbsoluteConstraints(184, 115, 22, 22));

        jLabel75.setFont(jLabel75.getFont().deriveFont(jLabel75.getFont().getSize()-5f));
        jLabel75.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel75.setText("37-29");
        jLabel75.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        add(jLabel75, new org.netbeans.lib.awtextra.AbsoluteConstraints(207, 115, 22, 22));

        jLabel76.setFont(jLabel76.getFont().deriveFont(jLabel76.getFont().getSize()-5f));
        jLabel76.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel76.setText("37-30");
        jLabel76.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        add(jLabel76, new org.netbeans.lib.awtextra.AbsoluteConstraints(230, 115, 22, 22));

        jLabel77.setFont(jLabel77.getFont().deriveFont(jLabel77.getFont().getSize()-5f));
        jLabel77.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel77.setText("37-31");
        jLabel77.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        add(jLabel77, new org.netbeans.lib.awtextra.AbsoluteConstraints(253, 115, 22, 22));

        jLabel78.setFont(jLabel78.getFont().deriveFont(jLabel78.getFont().getSize()-5f));
        jLabel78.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel78.setText("37-32");
        jLabel78.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        add(jLabel78, new org.netbeans.lib.awtextra.AbsoluteConstraints(276, 115, 22, 22));

        jLabel79.setFont(jLabel79.getFont().deriveFont(jLabel79.getFont().getSize()-5f));
        jLabel79.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel79.setText("37-33");
        jLabel79.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        add(jLabel79, new org.netbeans.lib.awtextra.AbsoluteConstraints(299, 115, 22, 22));

        jLabel80.setFont(jLabel80.getFont().deriveFont(jLabel80.getFont().getSize()-5f));
        jLabel80.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel80.setText("37-34");
        jLabel80.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        add(jLabel80, new org.netbeans.lib.awtextra.AbsoluteConstraints(322, 115, 22, 22));

        jLabel81.setFont(jLabel81.getFont().deriveFont(jLabel81.getFont().getSize()-5f));
        jLabel81.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel81.setText("37-35");
        jLabel81.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        add(jLabel81, new org.netbeans.lib.awtextra.AbsoluteConstraints(345, 115, 22, 22));

        jLabel82.setFont(jLabel82.getFont().deriveFont(jLabel82.getFont().getSize()-5f));
        jLabel82.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel82.setText("37-36");
        jLabel82.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        add(jLabel82, new org.netbeans.lib.awtextra.AbsoluteConstraints(368, 115, 22, 22));

        jLabel83.setFont(jLabel83.getFont().deriveFont(jLabel83.getFont().getSize()-5f));
        jLabel83.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel83.setText("37-37");
        jLabel83.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        add(jLabel83, new org.netbeans.lib.awtextra.AbsoluteConstraints(391, 115, 22, 22));

        jLabel84.setFont(jLabel84.getFont().deriveFont(jLabel84.getFont().getSize()-5f));
        jLabel84.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel84.setText("37-38");
        jLabel84.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        add(jLabel84, new org.netbeans.lib.awtextra.AbsoluteConstraints(414, 115, 22, 22));

        jLabel85.setFont(jLabel85.getFont().deriveFont(jLabel85.getFont().getSize()-5f));
        jLabel85.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel85.setText("37-39");
        jLabel85.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        add(jLabel85, new org.netbeans.lib.awtextra.AbsoluteConstraints(437, 115, 22, 22));

        jLabel86.setFont(jLabel86.getFont().deriveFont(jLabel86.getFont().getSize()-5f));
        jLabel86.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel86.setText("37-40");
        jLabel86.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        add(jLabel86, new org.netbeans.lib.awtextra.AbsoluteConstraints(460, 115, 22, 22));

        jLabel87.setFont(jLabel87.getFont().deriveFont(jLabel87.getFont().getSize()-5f));
        jLabel87.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel87.setText("36-21");
        jLabel87.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        add(jLabel87, new org.netbeans.lib.awtextra.AbsoluteConstraints(23, 138, 22, 22));

        jLabel88.setFont(jLabel88.getFont().deriveFont(jLabel88.getFont().getSize()-5f));
        jLabel88.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel88.setText("36-22");
        jLabel88.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        add(jLabel88, new org.netbeans.lib.awtextra.AbsoluteConstraints(46, 138, 22, 22));

        jLabel89.setFont(jLabel89.getFont().deriveFont(jLabel89.getFont().getSize()-5f));
        jLabel89.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel89.setText("36-23");
        jLabel89.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        add(jLabel89, new org.netbeans.lib.awtextra.AbsoluteConstraints(69, 138, 22, 22));

        jLabel90.setFont(jLabel90.getFont().deriveFont(jLabel90.getFont().getSize()-5f));
        jLabel90.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel90.setText("36-24");
        jLabel90.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        add(jLabel90, new org.netbeans.lib.awtextra.AbsoluteConstraints(92, 138, 22, 22));

        jLabel91.setFont(jLabel91.getFont().deriveFont(jLabel91.getFont().getSize()-5f));
        jLabel91.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel91.setText("36-25");
        jLabel91.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        add(jLabel91, new org.netbeans.lib.awtextra.AbsoluteConstraints(115, 138, 22, 22));

        jLabel92.setFont(jLabel92.getFont().deriveFont(jLabel92.getFont().getSize()-5f));
        jLabel92.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel92.setText("36-26");
        jLabel92.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        add(jLabel92, new org.netbeans.lib.awtextra.AbsoluteConstraints(138, 138, 22, 22));

        jLabel93.setFont(jLabel93.getFont().deriveFont(jLabel93.getFont().getSize()-5f));
        jLabel93.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel93.setText("36-27");
        jLabel93.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        add(jLabel93, new org.netbeans.lib.awtextra.AbsoluteConstraints(161, 138, 22, 22));

        jLabel94.setFont(jLabel94.getFont().deriveFont(jLabel94.getFont().getSize()-5f));
        jLabel94.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel94.setText("36-28");
        jLabel94.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        add(jLabel94, new org.netbeans.lib.awtextra.AbsoluteConstraints(184, 138, 22, 22));

        jLabel95.setFont(jLabel95.getFont().deriveFont(jLabel95.getFont().getSize()-5f));
        jLabel95.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel95.setText("36-29");
        jLabel95.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        add(jLabel95, new org.netbeans.lib.awtextra.AbsoluteConstraints(207, 138, 22, 22));

        jLabel96.setFont(jLabel96.getFont().deriveFont(jLabel96.getFont().getSize()-5f));
        jLabel96.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel96.setText("36-30");
        jLabel96.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        add(jLabel96, new org.netbeans.lib.awtextra.AbsoluteConstraints(230, 138, 22, 22));

        jLabel97.setFont(jLabel97.getFont().deriveFont(jLabel97.getFont().getSize()-5f));
        jLabel97.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel97.setText("36-31");
        jLabel97.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        add(jLabel97, new org.netbeans.lib.awtextra.AbsoluteConstraints(253, 138, 22, 22));

        jLabel98.setFont(jLabel98.getFont().deriveFont(jLabel98.getFont().getSize()-5f));
        jLabel98.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel98.setText("36-32");
        jLabel98.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        add(jLabel98, new org.netbeans.lib.awtextra.AbsoluteConstraints(276, 138, 22, 22));

        jLabel99.setFont(jLabel99.getFont().deriveFont(jLabel99.getFont().getSize()-5f));
        jLabel99.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel99.setText("36-33");
        jLabel99.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        add(jLabel99, new org.netbeans.lib.awtextra.AbsoluteConstraints(299, 138, 22, 22));

        jLabel100.setFont(jLabel100.getFont().deriveFont(jLabel100.getFont().getSize()-5f));
        jLabel100.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel100.setText("36-34");
        jLabel100.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        add(jLabel100, new org.netbeans.lib.awtextra.AbsoluteConstraints(322, 138, 22, 22));

        jLabel101.setFont(jLabel101.getFont().deriveFont(jLabel101.getFont().getSize()-5f));
        jLabel101.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel101.setText("36-35");
        jLabel101.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        add(jLabel101, new org.netbeans.lib.awtextra.AbsoluteConstraints(345, 138, 22, 22));

        jLabel102.setFont(jLabel102.getFont().deriveFont(jLabel102.getFont().getSize()-5f));
        jLabel102.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel102.setText("36-36");
        jLabel102.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        add(jLabel102, new org.netbeans.lib.awtextra.AbsoluteConstraints(368, 138, 22, 22));

        jLabel103.setFont(jLabel103.getFont().deriveFont(jLabel103.getFont().getSize()-5f));
        jLabel103.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel103.setText("36-37");
        jLabel103.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        add(jLabel103, new org.netbeans.lib.awtextra.AbsoluteConstraints(391, 138, 22, 22));

        jLabel104.setFont(jLabel104.getFont().deriveFont(jLabel104.getFont().getSize()-5f));
        jLabel104.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel104.setText("36-38");
        jLabel104.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        add(jLabel104, new org.netbeans.lib.awtextra.AbsoluteConstraints(414, 138, 22, 22));

        jLabel105.setFont(jLabel105.getFont().deriveFont(jLabel105.getFont().getSize()-5f));
        jLabel105.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel105.setText("36-39");
        jLabel105.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        add(jLabel105, new org.netbeans.lib.awtextra.AbsoluteConstraints(437, 138, 22, 22));

        jLabel106.setFont(jLabel106.getFont().deriveFont(jLabel106.getFont().getSize()-5f));
        jLabel106.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel106.setText("36-40");
        jLabel106.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        add(jLabel106, new org.netbeans.lib.awtextra.AbsoluteConstraints(460, 138, 22, 22));

        jLabel107.setFont(jLabel107.getFont().deriveFont(jLabel107.getFont().getSize()-5f));
        jLabel107.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel107.setText("36-41");
        jLabel107.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        add(jLabel107, new org.netbeans.lib.awtextra.AbsoluteConstraints(483, 138, 22, 22));

        jLabel108.setFont(jLabel108.getFont().deriveFont(jLabel108.getFont().getSize()-5f));
        jLabel108.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel108.setText("35-21");
        jLabel108.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        add(jLabel108, new org.netbeans.lib.awtextra.AbsoluteConstraints(23, 161, 22, 22));

        jLabel109.setFont(jLabel109.getFont().deriveFont(jLabel109.getFont().getSize()-5f));
        jLabel109.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel109.setText("35-22");
        jLabel109.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        add(jLabel109, new org.netbeans.lib.awtextra.AbsoluteConstraints(46, 161, 22, 22));

        jLabel110.setFont(jLabel110.getFont().deriveFont(jLabel110.getFont().getSize()-5f));
        jLabel110.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel110.setText("35-23");
        jLabel110.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        add(jLabel110, new org.netbeans.lib.awtextra.AbsoluteConstraints(69, 161, 22, 22));

        jLabel111.setFont(jLabel111.getFont().deriveFont(jLabel111.getFont().getSize()-5f));
        jLabel111.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel111.setText("35-24");
        jLabel111.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        add(jLabel111, new org.netbeans.lib.awtextra.AbsoluteConstraints(92, 161, 22, 22));

        jLabel112.setFont(jLabel112.getFont().deriveFont(jLabel112.getFont().getSize()-5f));
        jLabel112.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel112.setText("35-25");
        jLabel112.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        add(jLabel112, new org.netbeans.lib.awtextra.AbsoluteConstraints(115, 161, 22, 22));

        jLabel113.setFont(jLabel113.getFont().deriveFont(jLabel113.getFont().getSize()-5f));
        jLabel113.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel113.setText("35-26");
        jLabel113.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        add(jLabel113, new org.netbeans.lib.awtextra.AbsoluteConstraints(138, 161, 22, 22));

        jLabel114.setFont(jLabel114.getFont().deriveFont(jLabel114.getFont().getSize()-5f));
        jLabel114.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel114.setText("35-27");
        jLabel114.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        add(jLabel114, new org.netbeans.lib.awtextra.AbsoluteConstraints(161, 161, 22, 22));

        jLabel115.setFont(jLabel115.getFont().deriveFont(jLabel115.getFont().getSize()-5f));
        jLabel115.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel115.setText("35-28");
        jLabel115.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        add(jLabel115, new org.netbeans.lib.awtextra.AbsoluteConstraints(184, 161, 22, 22));

        jLabel116.setFont(jLabel116.getFont().deriveFont(jLabel116.getFont().getSize()-5f));
        jLabel116.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel116.setText("35-29");
        jLabel116.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        add(jLabel116, new org.netbeans.lib.awtextra.AbsoluteConstraints(207, 161, 22, 22));

        jLabel117.setFont(jLabel117.getFont().deriveFont(jLabel117.getFont().getSize()-5f));
        jLabel117.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel117.setText("35-30");
        jLabel117.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        add(jLabel117, new org.netbeans.lib.awtextra.AbsoluteConstraints(230, 161, 22, 22));

        jLabel118.setFont(jLabel118.getFont().deriveFont(jLabel118.getFont().getSize()-5f));
        jLabel118.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel118.setText("35-31");
        jLabel118.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        add(jLabel118, new org.netbeans.lib.awtextra.AbsoluteConstraints(253, 161, 22, 22));

        jLabel119.setFont(jLabel119.getFont().deriveFont(jLabel119.getFont().getSize()-5f));
        jLabel119.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel119.setText("35-32");
        jLabel119.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        add(jLabel119, new org.netbeans.lib.awtextra.AbsoluteConstraints(276, 161, 22, 22));

        jLabel120.setFont(jLabel120.getFont().deriveFont(jLabel120.getFont().getSize()-5f));
        jLabel120.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel120.setText("35-33");
        jLabel120.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        add(jLabel120, new org.netbeans.lib.awtextra.AbsoluteConstraints(299, 161, 22, 22));

        jLabel121.setFont(jLabel121.getFont().deriveFont(jLabel121.getFont().getSize()-5f));
        jLabel121.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel121.setText("35-34");
        jLabel121.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        add(jLabel121, new org.netbeans.lib.awtextra.AbsoluteConstraints(322, 161, 22, 22));

        jLabel122.setFont(jLabel122.getFont().deriveFont(jLabel122.getFont().getSize()-5f));
        jLabel122.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel122.setText("35-35");
        jLabel122.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        add(jLabel122, new org.netbeans.lib.awtextra.AbsoluteConstraints(345, 161, 22, 22));

        jLabel123.setFont(jLabel123.getFont().deriveFont(jLabel123.getFont().getSize()-5f));
        jLabel123.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel123.setText("35-36");
        jLabel123.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        add(jLabel123, new org.netbeans.lib.awtextra.AbsoluteConstraints(368, 161, 22, 22));

        jLabel124.setFont(jLabel124.getFont().deriveFont(jLabel124.getFont().getSize()-5f));
        jLabel124.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel124.setText("35-37");
        jLabel124.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        add(jLabel124, new org.netbeans.lib.awtextra.AbsoluteConstraints(391, 161, 22, 22));

        jLabel125.setFont(jLabel125.getFont().deriveFont(jLabel125.getFont().getSize()-5f));
        jLabel125.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel125.setText("35-38");
        jLabel125.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        add(jLabel125, new org.netbeans.lib.awtextra.AbsoluteConstraints(414, 161, 22, 22));

        jLabel126.setFont(jLabel126.getFont().deriveFont(jLabel126.getFont().getSize()-5f));
        jLabel126.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel126.setText("35-39");
        jLabel126.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        add(jLabel126, new org.netbeans.lib.awtextra.AbsoluteConstraints(437, 161, 22, 22));

        jLabel127.setFont(jLabel127.getFont().deriveFont(jLabel127.getFont().getSize()-5f));
        jLabel127.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel127.setText("35-40");
        jLabel127.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        add(jLabel127, new org.netbeans.lib.awtextra.AbsoluteConstraints(460, 161, 22, 22));

        jLabel128.setFont(jLabel128.getFont().deriveFont(jLabel128.getFont().getSize()-5f));
        jLabel128.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel128.setText("35-41");
        jLabel128.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        add(jLabel128, new org.netbeans.lib.awtextra.AbsoluteConstraints(483, 161, 22, 22));

        jLabel129.setFont(jLabel129.getFont().deriveFont(jLabel129.getFont().getSize()-5f));
        jLabel129.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel129.setText("34-21");
        jLabel129.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        add(jLabel129, new org.netbeans.lib.awtextra.AbsoluteConstraints(23, 184, 22, 22));

        jLabel130.setFont(jLabel130.getFont().deriveFont(jLabel130.getFont().getSize()-5f));
        jLabel130.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel130.setText("34-22");
        jLabel130.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        add(jLabel130, new org.netbeans.lib.awtextra.AbsoluteConstraints(46, 184, 22, 22));

        jLabel131.setFont(jLabel131.getFont().deriveFont(jLabel131.getFont().getSize()-5f));
        jLabel131.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel131.setText("34-23");
        jLabel131.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        add(jLabel131, new org.netbeans.lib.awtextra.AbsoluteConstraints(69, 184, 22, 22));

        jLabel132.setFont(jLabel132.getFont().deriveFont(jLabel132.getFont().getSize()-5f));
        jLabel132.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel132.setText("34-24");
        jLabel132.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        add(jLabel132, new org.netbeans.lib.awtextra.AbsoluteConstraints(92, 184, 22, 22));

        jLabel133.setFont(jLabel133.getFont().deriveFont(jLabel133.getFont().getSize()-5f));
        jLabel133.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel133.setText("34-25");
        jLabel133.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        add(jLabel133, new org.netbeans.lib.awtextra.AbsoluteConstraints(115, 184, 22, 22));

        jLabel134.setFont(jLabel134.getFont().deriveFont(jLabel134.getFont().getSize()-5f));
        jLabel134.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel134.setText("34-26");
        jLabel134.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        add(jLabel134, new org.netbeans.lib.awtextra.AbsoluteConstraints(138, 184, 22, 22));

        jLabel135.setFont(jLabel135.getFont().deriveFont(jLabel135.getFont().getSize()-5f));
        jLabel135.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel135.setText("34-27");
        jLabel135.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        add(jLabel135, new org.netbeans.lib.awtextra.AbsoluteConstraints(161, 184, 22, 22));

        jLabel136.setFont(jLabel136.getFont().deriveFont(jLabel136.getFont().getSize()-5f));
        jLabel136.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel136.setText("34-28");
        jLabel136.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        add(jLabel136, new org.netbeans.lib.awtextra.AbsoluteConstraints(184, 184, 22, 22));

        jLabel137.setFont(jLabel137.getFont().deriveFont(jLabel137.getFont().getSize()-5f));
        jLabel137.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel137.setText("34-29");
        jLabel137.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        add(jLabel137, new org.netbeans.lib.awtextra.AbsoluteConstraints(207, 184, 22, 22));

        jLabel138.setFont(jLabel138.getFont().deriveFont(jLabel138.getFont().getSize()-5f));
        jLabel138.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel138.setText("34-30");
        jLabel138.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        add(jLabel138, new org.netbeans.lib.awtextra.AbsoluteConstraints(230, 184, 22, 22));

        jLabel139.setFont(jLabel139.getFont().deriveFont(jLabel139.getFont().getSize()-5f));
        jLabel139.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel139.setText("34-31");
        jLabel139.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        add(jLabel139, new org.netbeans.lib.awtextra.AbsoluteConstraints(253, 184, 22, 22));

        jLabel140.setFont(jLabel140.getFont().deriveFont(jLabel140.getFont().getSize()-5f));
        jLabel140.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel140.setText("34-32");
        jLabel140.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        add(jLabel140, new org.netbeans.lib.awtextra.AbsoluteConstraints(276, 184, 22, 22));

        jLabel141.setFont(jLabel141.getFont().deriveFont(jLabel141.getFont().getSize()-5f));
        jLabel141.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel141.setText("34-33");
        jLabel141.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        add(jLabel141, new org.netbeans.lib.awtextra.AbsoluteConstraints(299, 184, 22, 22));

        jLabel142.setFont(jLabel142.getFont().deriveFont(jLabel142.getFont().getSize()-5f));
        jLabel142.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel142.setText("34-34");
        jLabel142.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        add(jLabel142, new org.netbeans.lib.awtextra.AbsoluteConstraints(322, 184, 22, 22));

        jLabel143.setFont(jLabel143.getFont().deriveFont(jLabel143.getFont().getSize()-5f));
        jLabel143.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel143.setText("34-35");
        jLabel143.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        add(jLabel143, new org.netbeans.lib.awtextra.AbsoluteConstraints(345, 184, 22, 22));

        jLabel144.setFont(jLabel144.getFont().deriveFont(jLabel144.getFont().getSize()-5f));
        jLabel144.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel144.setText("34-36");
        jLabel144.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        add(jLabel144, new org.netbeans.lib.awtextra.AbsoluteConstraints(368, 184, 22, 22));

        jLabel145.setFont(jLabel145.getFont().deriveFont(jLabel145.getFont().getSize()-5f));
        jLabel145.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel145.setText("34-37");
        jLabel145.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        add(jLabel145, new org.netbeans.lib.awtextra.AbsoluteConstraints(391, 184, 22, 22));

        jLabel146.setFont(jLabel146.getFont().deriveFont(jLabel146.getFont().getSize()-5f));
        jLabel146.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel146.setText("34-38");
        jLabel146.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        add(jLabel146, new org.netbeans.lib.awtextra.AbsoluteConstraints(414, 184, 22, 22));

        jLabel147.setFont(jLabel147.getFont().deriveFont(jLabel147.getFont().getSize()-5f));
        jLabel147.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel147.setText("34-39");
        jLabel147.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        add(jLabel147, new org.netbeans.lib.awtextra.AbsoluteConstraints(437, 184, 22, 22));

        jLabel148.setFont(jLabel148.getFont().deriveFont(jLabel148.getFont().getSize()-5f));
        jLabel148.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel148.setText("34-40");
        jLabel148.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        add(jLabel148, new org.netbeans.lib.awtextra.AbsoluteConstraints(460, 184, 22, 22));

        jLabel149.setFont(jLabel149.getFont().deriveFont(jLabel149.getFont().getSize()-5f));
        jLabel149.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel149.setText("34-41");
        jLabel149.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        add(jLabel149, new org.netbeans.lib.awtextra.AbsoluteConstraints(483, 184, 22, 22));

        jLabel150.setFont(jLabel150.getFont().deriveFont(jLabel150.getFont().getSize()-5f));
        jLabel150.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel150.setText("33-20");
        jLabel150.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        add(jLabel150, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 207, 22, 22));

        jLabel151.setFont(jLabel151.getFont().deriveFont(jLabel151.getFont().getSize()-5f));
        jLabel151.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel151.setText("33-21");
        jLabel151.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        add(jLabel151, new org.netbeans.lib.awtextra.AbsoluteConstraints(23, 207, 22, 22));

        jLabel152.setFont(jLabel152.getFont().deriveFont(jLabel152.getFont().getSize()-5f));
        jLabel152.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel152.setText("33-22");
        jLabel152.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        add(jLabel152, new org.netbeans.lib.awtextra.AbsoluteConstraints(46, 207, 22, 22));

        jLabel153.setFont(jLabel153.getFont().deriveFont(jLabel153.getFont().getSize()-5f));
        jLabel153.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel153.setText("33-23");
        jLabel153.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        add(jLabel153, new org.netbeans.lib.awtextra.AbsoluteConstraints(69, 207, 22, 22));

        jLabel154.setFont(jLabel154.getFont().deriveFont(jLabel154.getFont().getSize()-5f));
        jLabel154.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel154.setText("33-24");
        jLabel154.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        add(jLabel154, new org.netbeans.lib.awtextra.AbsoluteConstraints(92, 207, 22, 22));

        jLabel155.setFont(jLabel155.getFont().deriveFont(jLabel155.getFont().getSize()-5f));
        jLabel155.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel155.setText("33-25");
        jLabel155.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        add(jLabel155, new org.netbeans.lib.awtextra.AbsoluteConstraints(115, 207, 22, 22));

        jLabel156.setFont(jLabel156.getFont().deriveFont(jLabel156.getFont().getSize()-5f));
        jLabel156.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel156.setText("33-26");
        jLabel156.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        add(jLabel156, new org.netbeans.lib.awtextra.AbsoluteConstraints(138, 207, 22, 22));

        jLabel157.setFont(jLabel157.getFont().deriveFont(jLabel157.getFont().getSize()-5f));
        jLabel157.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel157.setText("33-27");
        jLabel157.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        add(jLabel157, new org.netbeans.lib.awtextra.AbsoluteConstraints(161, 207, 22, 22));

        jLabel158.setFont(jLabel158.getFont().deriveFont(jLabel158.getFont().getSize()-5f));
        jLabel158.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel158.setText("33-28");
        jLabel158.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        add(jLabel158, new org.netbeans.lib.awtextra.AbsoluteConstraints(184, 207, 22, 22));

        jLabel159.setFont(jLabel159.getFont().deriveFont(jLabel159.getFont().getSize()-5f));
        jLabel159.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel159.setText("33-29");
        jLabel159.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        add(jLabel159, new org.netbeans.lib.awtextra.AbsoluteConstraints(207, 207, 22, 22));

        jLabel160.setFont(jLabel160.getFont().deriveFont(jLabel160.getFont().getSize()-5f));
        jLabel160.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel160.setText("33-30");
        jLabel160.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        add(jLabel160, new org.netbeans.lib.awtextra.AbsoluteConstraints(230, 207, 22, 22));

        jLabel161.setFont(jLabel161.getFont().deriveFont(jLabel161.getFont().getSize()-5f));
        jLabel161.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel161.setText("33-31");
        jLabel161.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        add(jLabel161, new org.netbeans.lib.awtextra.AbsoluteConstraints(253, 207, 22, 22));

        jLabel162.setFont(jLabel162.getFont().deriveFont(jLabel162.getFont().getSize()-5f));
        jLabel162.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel162.setText("33-32");
        jLabel162.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        add(jLabel162, new org.netbeans.lib.awtextra.AbsoluteConstraints(276, 207, 22, 22));

        jLabel163.setFont(jLabel163.getFont().deriveFont(jLabel163.getFont().getSize()-5f));
        jLabel163.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel163.setText("33-33");
        jLabel163.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        add(jLabel163, new org.netbeans.lib.awtextra.AbsoluteConstraints(299, 207, 22, 22));

        jLabel164.setFont(jLabel164.getFont().deriveFont(jLabel164.getFont().getSize()-5f));
        jLabel164.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel164.setText("33-34");
        jLabel164.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        add(jLabel164, new org.netbeans.lib.awtextra.AbsoluteConstraints(322, 207, 22, 22));

        jLabel165.setFont(jLabel165.getFont().deriveFont(jLabel165.getFont().getSize()-5f));
        jLabel165.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel165.setText("33-35");
        jLabel165.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        add(jLabel165, new org.netbeans.lib.awtextra.AbsoluteConstraints(345, 207, 22, 22));

        jLabel166.setFont(jLabel166.getFont().deriveFont(jLabel166.getFont().getSize()-5f));
        jLabel166.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel166.setText("33-36");
        jLabel166.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        add(jLabel166, new org.netbeans.lib.awtextra.AbsoluteConstraints(368, 207, 22, 22));

        jLabel167.setFont(jLabel167.getFont().deriveFont(jLabel167.getFont().getSize()-5f));
        jLabel167.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel167.setText("33-37");
        jLabel167.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        add(jLabel167, new org.netbeans.lib.awtextra.AbsoluteConstraints(391, 207, 22, 22));

        jLabel168.setFont(jLabel168.getFont().deriveFont(jLabel168.getFont().getSize()-5f));
        jLabel168.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel168.setText("33-38");
        jLabel168.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        add(jLabel168, new org.netbeans.lib.awtextra.AbsoluteConstraints(414, 207, 22, 22));

        jLabel169.setFont(jLabel169.getFont().deriveFont(jLabel169.getFont().getSize()-5f));
        jLabel169.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel169.setText("33-39");
        jLabel169.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        add(jLabel169, new org.netbeans.lib.awtextra.AbsoluteConstraints(437, 207, 22, 22));

        jLabel170.setFont(jLabel170.getFont().deriveFont(jLabel170.getFont().getSize()-5f));
        jLabel170.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel170.setText("33-40");
        jLabel170.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        add(jLabel170, new org.netbeans.lib.awtextra.AbsoluteConstraints(460, 207, 22, 22));

        jLabel171.setFont(jLabel171.getFont().deriveFont(jLabel171.getFont().getSize()-5f));
        jLabel171.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel171.setText("33-41");
        jLabel171.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        add(jLabel171, new org.netbeans.lib.awtextra.AbsoluteConstraints(483, 207, 22, 22));

        jLabel172.setFont(jLabel172.getFont().deriveFont(jLabel172.getFont().getSize()-5f));
        jLabel172.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel172.setText("33-42");
        jLabel172.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        add(jLabel172, new org.netbeans.lib.awtextra.AbsoluteConstraints(506, 207, 22, 22));

        jLabel173.setFont(jLabel173.getFont().deriveFont(jLabel173.getFont().getSize()-5f));
        jLabel173.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel173.setText("32-20");
        jLabel173.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        add(jLabel173, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 230, 22, 22));

        jLabel174.setFont(jLabel174.getFont().deriveFont(jLabel174.getFont().getSize()-5f));
        jLabel174.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel174.setText("32-21");
        jLabel174.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        add(jLabel174, new org.netbeans.lib.awtextra.AbsoluteConstraints(23, 230, 22, 22));

        jLabel175.setFont(jLabel175.getFont().deriveFont(jLabel175.getFont().getSize()-5f));
        jLabel175.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel175.setText("32-22");
        jLabel175.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        add(jLabel175, new org.netbeans.lib.awtextra.AbsoluteConstraints(46, 230, 22, 22));

        jLabel176.setFont(jLabel176.getFont().deriveFont(jLabel176.getFont().getSize()-5f));
        jLabel176.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel176.setText("32-23");
        jLabel176.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        add(jLabel176, new org.netbeans.lib.awtextra.AbsoluteConstraints(69, 230, 22, 22));

        jLabel177.setFont(jLabel177.getFont().deriveFont(jLabel177.getFont().getSize()-5f));
        jLabel177.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel177.setText("32-24");
        jLabel177.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        add(jLabel177, new org.netbeans.lib.awtextra.AbsoluteConstraints(92, 230, 22, 22));

        jLabel178.setFont(jLabel178.getFont().deriveFont(jLabel178.getFont().getSize()-5f));
        jLabel178.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel178.setText("32-25");
        jLabel178.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        add(jLabel178, new org.netbeans.lib.awtextra.AbsoluteConstraints(115, 230, 22, 22));

        jLabel179.setFont(jLabel179.getFont().deriveFont(jLabel179.getFont().getSize()-5f));
        jLabel179.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel179.setText("32-26");
        jLabel179.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        add(jLabel179, new org.netbeans.lib.awtextra.AbsoluteConstraints(138, 230, 22, 22));

        jLabel180.setFont(jLabel180.getFont().deriveFont(jLabel180.getFont().getSize()-5f));
        jLabel180.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel180.setText("32-27");
        jLabel180.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        add(jLabel180, new org.netbeans.lib.awtextra.AbsoluteConstraints(161, 230, 22, 22));

        jLabel181.setFont(jLabel181.getFont().deriveFont(jLabel181.getFont().getSize()-5f));
        jLabel181.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel181.setText("32-28");
        jLabel181.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        add(jLabel181, new org.netbeans.lib.awtextra.AbsoluteConstraints(184, 230, 22, 22));

        jLabel182.setFont(jLabel182.getFont().deriveFont(jLabel182.getFont().getSize()-5f));
        jLabel182.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel182.setText("32-29");
        jLabel182.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        add(jLabel182, new org.netbeans.lib.awtextra.AbsoluteConstraints(207, 230, 22, 22));

        jLabel183.setFont(jLabel183.getFont().deriveFont(jLabel183.getFont().getSize()-5f));
        jLabel183.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel183.setText("32-30");
        jLabel183.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        add(jLabel183, new org.netbeans.lib.awtextra.AbsoluteConstraints(230, 230, 22, 22));

        jLabel184.setFont(jLabel184.getFont().deriveFont(jLabel184.getFont().getSize()-5f));
        jLabel184.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel184.setText("32-31");
        jLabel184.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        add(jLabel184, new org.netbeans.lib.awtextra.AbsoluteConstraints(253, 230, 22, 22));

        jLabel185.setFont(jLabel185.getFont().deriveFont(jLabel185.getFont().getSize()-5f));
        jLabel185.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel185.setText("32-32");
        jLabel185.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        add(jLabel185, new org.netbeans.lib.awtextra.AbsoluteConstraints(276, 230, 22, 22));

        jLabel186.setFont(jLabel186.getFont().deriveFont(jLabel186.getFont().getSize()-5f));
        jLabel186.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel186.setText("32-33");
        jLabel186.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        add(jLabel186, new org.netbeans.lib.awtextra.AbsoluteConstraints(299, 230, 22, 22));

        jLabel187.setFont(jLabel187.getFont().deriveFont(jLabel187.getFont().getSize()-5f));
        jLabel187.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel187.setText("32-34");
        jLabel187.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        add(jLabel187, new org.netbeans.lib.awtextra.AbsoluteConstraints(322, 230, 22, 22));

        jLabel188.setFont(jLabel188.getFont().deriveFont(jLabel188.getFont().getSize()-5f));
        jLabel188.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel188.setText("32-35");
        jLabel188.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        add(jLabel188, new org.netbeans.lib.awtextra.AbsoluteConstraints(345, 230, 22, 22));

        jLabel189.setFont(jLabel189.getFont().deriveFont(jLabel189.getFont().getSize()-5f));
        jLabel189.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel189.setText("32-36");
        jLabel189.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        add(jLabel189, new org.netbeans.lib.awtextra.AbsoluteConstraints(368, 230, 22, 22));

        jLabel190.setFont(jLabel190.getFont().deriveFont(jLabel190.getFont().getSize()-5f));
        jLabel190.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel190.setText("32-37");
        jLabel190.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        add(jLabel190, new org.netbeans.lib.awtextra.AbsoluteConstraints(391, 230, 22, 22));

        jLabel191.setFont(jLabel191.getFont().deriveFont(jLabel191.getFont().getSize()-5f));
        jLabel191.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel191.setText("32-38");
        jLabel191.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        add(jLabel191, new org.netbeans.lib.awtextra.AbsoluteConstraints(414, 230, 22, 22));

        jLabel192.setFont(jLabel192.getFont().deriveFont(jLabel192.getFont().getSize()-5f));
        jLabel192.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel192.setText("32-39");
        jLabel192.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        add(jLabel192, new org.netbeans.lib.awtextra.AbsoluteConstraints(437, 230, 22, 22));

        jLabel193.setFont(jLabel193.getFont().deriveFont(jLabel193.getFont().getSize()-5f));
        jLabel193.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel193.setText("32-40");
        jLabel193.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        add(jLabel193, new org.netbeans.lib.awtextra.AbsoluteConstraints(460, 230, 22, 22));

        jLabel194.setFont(jLabel194.getFont().deriveFont(jLabel194.getFont().getSize()-5f));
        jLabel194.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel194.setText("32-41");
        jLabel194.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        add(jLabel194, new org.netbeans.lib.awtextra.AbsoluteConstraints(483, 230, 22, 22));

        jLabel195.setFont(jLabel195.getFont().deriveFont(jLabel195.getFont().getSize()-5f));
        jLabel195.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel195.setText("32-42");
        jLabel195.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        add(jLabel195, new org.netbeans.lib.awtextra.AbsoluteConstraints(506, 230, 22, 22));

        jLabel196.setFont(jLabel196.getFont().deriveFont(jLabel196.getFont().getSize()-5f));
        jLabel196.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel196.setText("31-20");
        jLabel196.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        add(jLabel196, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 253, 22, 22));

        jLabel197.setFont(jLabel197.getFont().deriveFont(jLabel197.getFont().getSize()-5f));
        jLabel197.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel197.setText("31-21");
        jLabel197.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        add(jLabel197, new org.netbeans.lib.awtextra.AbsoluteConstraints(23, 253, 22, 22));

        jLabel198.setFont(jLabel198.getFont().deriveFont(jLabel198.getFont().getSize()-5f));
        jLabel198.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel198.setText("31-22");
        jLabel198.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        add(jLabel198, new org.netbeans.lib.awtextra.AbsoluteConstraints(46, 253, 22, 22));

        jLabel199.setFont(jLabel199.getFont().deriveFont(jLabel199.getFont().getSize()-5f));
        jLabel199.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel199.setText("31-23");
        jLabel199.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        add(jLabel199, new org.netbeans.lib.awtextra.AbsoluteConstraints(69, 253, 22, 22));

        jLabel200.setFont(jLabel200.getFont().deriveFont(jLabel200.getFont().getSize()-5f));
        jLabel200.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel200.setText("31-24");
        jLabel200.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        add(jLabel200, new org.netbeans.lib.awtextra.AbsoluteConstraints(92, 253, 22, 22));

        jLabel201.setFont(jLabel201.getFont().deriveFont(jLabel201.getFont().getSize()-5f));
        jLabel201.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel201.setText("31-25");
        jLabel201.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        add(jLabel201, new org.netbeans.lib.awtextra.AbsoluteConstraints(115, 253, 22, 22));

        jLabel202.setFont(jLabel202.getFont().deriveFont(jLabel202.getFont().getSize()-5f));
        jLabel202.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel202.setText("31-26");
        jLabel202.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        add(jLabel202, new org.netbeans.lib.awtextra.AbsoluteConstraints(138, 253, 22, 22));

        jLabel203.setFont(jLabel203.getFont().deriveFont(jLabel203.getFont().getSize()-5f));
        jLabel203.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel203.setText("31-27");
        jLabel203.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        add(jLabel203, new org.netbeans.lib.awtextra.AbsoluteConstraints(161, 253, 22, 22));

        jLabel204.setFont(jLabel204.getFont().deriveFont(jLabel204.getFont().getSize()-5f));
        jLabel204.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel204.setText("31-28");
        jLabel204.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        add(jLabel204, new org.netbeans.lib.awtextra.AbsoluteConstraints(184, 253, 22, 22));

        jLabel205.setFont(jLabel205.getFont().deriveFont(jLabel205.getFont().getSize()-5f));
        jLabel205.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel205.setText("31-29");
        jLabel205.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        add(jLabel205, new org.netbeans.lib.awtextra.AbsoluteConstraints(207, 253, 22, 22));

        jLabel206.setFont(jLabel206.getFont().deriveFont(jLabel206.getFont().getSize()-5f));
        jLabel206.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel206.setText("31-30");
        jLabel206.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        add(jLabel206, new org.netbeans.lib.awtextra.AbsoluteConstraints(230, 253, 22, 22));

        jLabel207.setFont(jLabel207.getFont().deriveFont(jLabel207.getFont().getSize()-5f));
        jLabel207.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel207.setText("31-31");
        jLabel207.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        add(jLabel207, new org.netbeans.lib.awtextra.AbsoluteConstraints(253, 253, 22, 22));

        jLabel208.setFont(jLabel208.getFont().deriveFont(jLabel208.getFont().getSize()-5f));
        jLabel208.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel208.setText("31-32");
        jLabel208.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        add(jLabel208, new org.netbeans.lib.awtextra.AbsoluteConstraints(276, 253, 22, 22));

        jLabel209.setFont(jLabel209.getFont().deriveFont(jLabel209.getFont().getSize()-5f));
        jLabel209.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel209.setText("31-33");
        jLabel209.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        add(jLabel209, new org.netbeans.lib.awtextra.AbsoluteConstraints(299, 253, 22, 22));

        jLabel210.setFont(jLabel210.getFont().deriveFont(jLabel210.getFont().getSize()-5f));
        jLabel210.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel210.setText("31-34");
        jLabel210.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        add(jLabel210, new org.netbeans.lib.awtextra.AbsoluteConstraints(322, 253, 22, 22));

        jLabel211.setFont(jLabel211.getFont().deriveFont(jLabel211.getFont().getSize()-5f));
        jLabel211.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel211.setText("31-35");
        jLabel211.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        add(jLabel211, new org.netbeans.lib.awtextra.AbsoluteConstraints(345, 253, 22, 22));

        jLabel212.setFont(jLabel212.getFont().deriveFont(jLabel212.getFont().getSize()-5f));
        jLabel212.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel212.setText("31-36");
        jLabel212.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        add(jLabel212, new org.netbeans.lib.awtextra.AbsoluteConstraints(368, 253, 22, 22));

        jLabel213.setFont(jLabel213.getFont().deriveFont(jLabel213.getFont().getSize()-5f));
        jLabel213.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel213.setText("31-37");
        jLabel213.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        add(jLabel213, new org.netbeans.lib.awtextra.AbsoluteConstraints(391, 253, 22, 22));

        jLabel214.setFont(jLabel214.getFont().deriveFont(jLabel214.getFont().getSize()-5f));
        jLabel214.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel214.setText("31-38");
        jLabel214.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        add(jLabel214, new org.netbeans.lib.awtextra.AbsoluteConstraints(414, 253, 22, 22));

        jLabel215.setFont(jLabel215.getFont().deriveFont(jLabel215.getFont().getSize()-5f));
        jLabel215.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel215.setText("31-39");
        jLabel215.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        add(jLabel215, new org.netbeans.lib.awtextra.AbsoluteConstraints(437, 253, 22, 22));

        jLabel216.setFont(jLabel216.getFont().deriveFont(jLabel216.getFont().getSize()-5f));
        jLabel216.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel216.setText("31-40");
        jLabel216.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        add(jLabel216, new org.netbeans.lib.awtextra.AbsoluteConstraints(460, 253, 22, 22));

        jLabel217.setFont(jLabel217.getFont().deriveFont(jLabel217.getFont().getSize()-5f));
        jLabel217.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel217.setText("31-41");
        jLabel217.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        add(jLabel217, new org.netbeans.lib.awtextra.AbsoluteConstraints(483, 253, 22, 22));

        jLabel218.setFont(jLabel218.getFont().deriveFont(jLabel218.getFont().getSize()-5f));
        jLabel218.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel218.setText("31-42");
        jLabel218.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        add(jLabel218, new org.netbeans.lib.awtextra.AbsoluteConstraints(506, 253, 22, 22));

        jLabel219.setFont(jLabel219.getFont().deriveFont(jLabel219.getFont().getSize()-5f));
        jLabel219.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel219.setText("30-20");
        jLabel219.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        add(jLabel219, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 276, 22, 22));

        jLabel220.setFont(jLabel220.getFont().deriveFont(jLabel220.getFont().getSize()-5f));
        jLabel220.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel220.setText("30-21");
        jLabel220.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        add(jLabel220, new org.netbeans.lib.awtextra.AbsoluteConstraints(23, 276, 22, 22));

        jLabel221.setFont(jLabel221.getFont().deriveFont(jLabel221.getFont().getSize()-5f));
        jLabel221.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel221.setText("30-22");
        jLabel221.setToolTipText("");
        jLabel221.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        add(jLabel221, new org.netbeans.lib.awtextra.AbsoluteConstraints(46, 276, 22, 22));

        jLabel222.setFont(jLabel222.getFont().deriveFont(jLabel222.getFont().getSize()-5f));
        jLabel222.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel222.setText("30-23");
        jLabel222.setToolTipText("");
        jLabel222.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        add(jLabel222, new org.netbeans.lib.awtextra.AbsoluteConstraints(69, 276, 22, 22));

        jLabel223.setFont(jLabel223.getFont().deriveFont(jLabel223.getFont().getSize()-5f));
        jLabel223.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel223.setText("30-24");
        jLabel223.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        add(jLabel223, new org.netbeans.lib.awtextra.AbsoluteConstraints(92, 276, 22, 22));

        jLabel224.setFont(jLabel224.getFont().deriveFont(jLabel224.getFont().getSize()-5f));
        jLabel224.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel224.setText("30-25");
        jLabel224.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        add(jLabel224, new org.netbeans.lib.awtextra.AbsoluteConstraints(115, 276, 22, 22));

        jLabel225.setFont(jLabel225.getFont().deriveFont(jLabel225.getFont().getSize()-5f));
        jLabel225.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel225.setText("30-26");
        jLabel225.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        add(jLabel225, new org.netbeans.lib.awtextra.AbsoluteConstraints(138, 276, 22, 22));

        jLabel226.setFont(jLabel226.getFont().deriveFont(jLabel226.getFont().getSize()-5f));
        jLabel226.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel226.setText("30-27");
        jLabel226.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        add(jLabel226, new org.netbeans.lib.awtextra.AbsoluteConstraints(161, 276, 22, 22));

        jLabel227.setFont(jLabel227.getFont().deriveFont(jLabel227.getFont().getSize()-5f));
        jLabel227.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel227.setText("30-28");
        jLabel227.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        add(jLabel227, new org.netbeans.lib.awtextra.AbsoluteConstraints(184, 276, 22, 22));

        jLabel228.setFont(jLabel228.getFont().deriveFont(jLabel228.getFont().getSize()-5f));
        jLabel228.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel228.setText("30-29");
        jLabel228.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        add(jLabel228, new org.netbeans.lib.awtextra.AbsoluteConstraints(207, 276, 22, 22));

        jLabel229.setFont(jLabel229.getFont().deriveFont(jLabel229.getFont().getSize()-5f));
        jLabel229.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel229.setText("30-30");
        jLabel229.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        add(jLabel229, new org.netbeans.lib.awtextra.AbsoluteConstraints(230, 276, 22, 22));

        jLabel230.setFont(jLabel230.getFont().deriveFont(jLabel230.getFont().getSize()-5f));
        jLabel230.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel230.setText("30-31");
        jLabel230.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        add(jLabel230, new org.netbeans.lib.awtextra.AbsoluteConstraints(253, 276, 22, 22));

        jLabel231.setFont(jLabel231.getFont().deriveFont(jLabel231.getFont().getSize()-5f));
        jLabel231.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel231.setText("30-32");
        jLabel231.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        add(jLabel231, new org.netbeans.lib.awtextra.AbsoluteConstraints(276, 276, 22, 22));

        jLabel232.setFont(jLabel232.getFont().deriveFont(jLabel232.getFont().getSize()-5f));
        jLabel232.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel232.setText("30-33");
        jLabel232.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        add(jLabel232, new org.netbeans.lib.awtextra.AbsoluteConstraints(299, 276, 22, 22));

        jLabel233.setFont(jLabel233.getFont().deriveFont(jLabel233.getFont().getSize()-5f));
        jLabel233.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel233.setText("30-34");
        jLabel233.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        add(jLabel233, new org.netbeans.lib.awtextra.AbsoluteConstraints(322, 276, 22, 22));

        jLabel234.setFont(jLabel234.getFont().deriveFont(jLabel234.getFont().getSize()-5f));
        jLabel234.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel234.setText("30-35");
        jLabel234.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        add(jLabel234, new org.netbeans.lib.awtextra.AbsoluteConstraints(345, 276, 22, 22));

        jLabel235.setFont(jLabel235.getFont().deriveFont(jLabel235.getFont().getSize()-5f));
        jLabel235.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel235.setText("30-36");
        jLabel235.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        add(jLabel235, new org.netbeans.lib.awtextra.AbsoluteConstraints(368, 276, 22, 22));

        jLabel236.setFont(jLabel236.getFont().deriveFont(jLabel236.getFont().getSize()-5f));
        jLabel236.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel236.setText("30-37");
        jLabel236.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        add(jLabel236, new org.netbeans.lib.awtextra.AbsoluteConstraints(391, 276, 22, 22));

        jLabel237.setFont(jLabel237.getFont().deriveFont(jLabel237.getFont().getSize()-5f));
        jLabel237.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel237.setText("30-38");
        jLabel237.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        add(jLabel237, new org.netbeans.lib.awtextra.AbsoluteConstraints(414, 276, 22, 22));

        jLabel238.setFont(jLabel238.getFont().deriveFont(jLabel238.getFont().getSize()-5f));
        jLabel238.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel238.setText("30-39");
        jLabel238.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        add(jLabel238, new org.netbeans.lib.awtextra.AbsoluteConstraints(437, 276, 22, 22));

        jLabel239.setFont(jLabel239.getFont().deriveFont(jLabel239.getFont().getSize()-5f));
        jLabel239.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel239.setText("30-40");
        jLabel239.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        add(jLabel239, new org.netbeans.lib.awtextra.AbsoluteConstraints(460, 276, 22, 22));

        jLabel240.setFont(jLabel240.getFont().deriveFont(jLabel240.getFont().getSize()-5f));
        jLabel240.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel240.setText("30-41");
        jLabel240.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        add(jLabel240, new org.netbeans.lib.awtextra.AbsoluteConstraints(483, 276, 22, 22));

        jLabel241.setFont(jLabel241.getFont().deriveFont(jLabel241.getFont().getSize()-5f));
        jLabel241.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel241.setText("30-42");
        jLabel241.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        add(jLabel241, new org.netbeans.lib.awtextra.AbsoluteConstraints(506, 276, 22, 22));

        jLabel242.setFont(jLabel242.getFont().deriveFont(jLabel242.getFont().getSize()-5f));
        jLabel242.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel242.setText("29-20");
        jLabel242.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        add(jLabel242, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 299, 22, 22));

        jLabel243.setFont(jLabel243.getFont().deriveFont(jLabel243.getFont().getSize()-5f));
        jLabel243.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel243.setText("29-21");
        jLabel243.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        add(jLabel243, new org.netbeans.lib.awtextra.AbsoluteConstraints(23, 299, 22, 22));

        jLabel244.setFont(jLabel244.getFont().deriveFont(jLabel244.getFont().getSize()-5f));
        jLabel244.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel244.setText("29-22");
        jLabel244.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        add(jLabel244, new org.netbeans.lib.awtextra.AbsoluteConstraints(46, 299, 22, 22));

        jLabel245.setFont(jLabel245.getFont().deriveFont(jLabel245.getFont().getSize()-5f));
        jLabel245.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel245.setText("29-23");
        jLabel245.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        add(jLabel245, new org.netbeans.lib.awtextra.AbsoluteConstraints(69, 299, 22, 22));

        jLabel246.setFont(jLabel246.getFont().deriveFont(jLabel246.getFont().getSize()-5f));
        jLabel246.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel246.setText("29-24");
        jLabel246.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        add(jLabel246, new org.netbeans.lib.awtextra.AbsoluteConstraints(92, 299, 22, 22));

        jLabel247.setFont(jLabel247.getFont().deriveFont(jLabel247.getFont().getSize()-5f));
        jLabel247.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel247.setText("29-25");
        jLabel247.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        add(jLabel247, new org.netbeans.lib.awtextra.AbsoluteConstraints(115, 299, 22, 22));

        jLabel248.setFont(jLabel248.getFont().deriveFont(jLabel248.getFont().getSize()-5f));
        jLabel248.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel248.setText("29-26");
        jLabel248.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        add(jLabel248, new org.netbeans.lib.awtextra.AbsoluteConstraints(138, 299, 22, 22));

        jLabel249.setFont(jLabel249.getFont().deriveFont(jLabel249.getFont().getSize()-5f));
        jLabel249.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel249.setText("29-27");
        jLabel249.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        add(jLabel249, new org.netbeans.lib.awtextra.AbsoluteConstraints(161, 299, 22, 22));

        jLabel250.setFont(jLabel250.getFont().deriveFont(jLabel250.getFont().getSize()-5f));
        jLabel250.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel250.setText("29-28");
        jLabel250.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        add(jLabel250, new org.netbeans.lib.awtextra.AbsoluteConstraints(184, 299, 22, 22));

        jLabel251.setFont(jLabel251.getFont().deriveFont(jLabel251.getFont().getSize()-5f));
        jLabel251.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel251.setText("29-29");
        jLabel251.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        add(jLabel251, new org.netbeans.lib.awtextra.AbsoluteConstraints(207, 299, 22, 22));

        jLabel252.setFont(jLabel252.getFont().deriveFont(jLabel252.getFont().getSize()-5f));
        jLabel252.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel252.setText("29-30");
        jLabel252.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        add(jLabel252, new org.netbeans.lib.awtextra.AbsoluteConstraints(230, 299, 22, 22));

        jLabel253.setFont(jLabel253.getFont().deriveFont(jLabel253.getFont().getSize()-5f));
        jLabel253.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel253.setText("29-31");
        jLabel253.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        add(jLabel253, new org.netbeans.lib.awtextra.AbsoluteConstraints(253, 299, 22, 22));

        jLabel254.setFont(jLabel254.getFont().deriveFont(jLabel254.getFont().getSize()-5f));
        jLabel254.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel254.setText("29-32");
        jLabel254.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        add(jLabel254, new org.netbeans.lib.awtextra.AbsoluteConstraints(276, 299, 22, 22));

        jLabel255.setFont(jLabel255.getFont().deriveFont(jLabel255.getFont().getSize()-5f));
        jLabel255.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel255.setText("29-33");
        jLabel255.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        add(jLabel255, new org.netbeans.lib.awtextra.AbsoluteConstraints(299, 299, 22, 22));

        jLabel256.setFont(jLabel256.getFont().deriveFont(jLabel256.getFont().getSize()-5f));
        jLabel256.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel256.setText("29-34");
        jLabel256.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        add(jLabel256, new org.netbeans.lib.awtextra.AbsoluteConstraints(322, 299, 22, 22));

        jLabel257.setFont(jLabel257.getFont().deriveFont(jLabel257.getFont().getSize()-5f));
        jLabel257.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel257.setText("29-35");
        jLabel257.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        add(jLabel257, new org.netbeans.lib.awtextra.AbsoluteConstraints(345, 299, 22, 22));

        jLabel258.setFont(jLabel258.getFont().deriveFont(jLabel258.getFont().getSize()-5f));
        jLabel258.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel258.setText("29-36");
        jLabel258.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        add(jLabel258, new org.netbeans.lib.awtextra.AbsoluteConstraints(368, 299, 22, 22));

        jLabel259.setFont(jLabel259.getFont().deriveFont(jLabel259.getFont().getSize()-5f));
        jLabel259.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel259.setText("29-37");
        jLabel259.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        add(jLabel259, new org.netbeans.lib.awtextra.AbsoluteConstraints(391, 299, 22, 22));

        jLabel260.setFont(jLabel260.getFont().deriveFont(jLabel260.getFont().getSize()-5f));
        jLabel260.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel260.setText("29-38");
        jLabel260.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        add(jLabel260, new org.netbeans.lib.awtextra.AbsoluteConstraints(414, 299, 22, 22));

        jLabel261.setFont(jLabel261.getFont().deriveFont(jLabel261.getFont().getSize()-5f));
        jLabel261.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel261.setText("29-39");
        jLabel261.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        add(jLabel261, new org.netbeans.lib.awtextra.AbsoluteConstraints(437, 299, 22, 22));

        jLabel262.setFont(jLabel262.getFont().deriveFont(jLabel262.getFont().getSize()-5f));
        jLabel262.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel262.setText("29-40");
        jLabel262.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        add(jLabel262, new org.netbeans.lib.awtextra.AbsoluteConstraints(460, 299, 22, 22));

        jLabel263.setFont(jLabel263.getFont().deriveFont(jLabel263.getFont().getSize()-5f));
        jLabel263.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel263.setText("29-41");
        jLabel263.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        add(jLabel263, new org.netbeans.lib.awtextra.AbsoluteConstraints(483, 299, 22, 22));

        jLabel264.setFont(jLabel264.getFont().deriveFont(jLabel264.getFont().getSize()-5f));
        jLabel264.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel264.setText("29-42");
        jLabel264.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        add(jLabel264, new org.netbeans.lib.awtextra.AbsoluteConstraints(506, 299, 22, 22));

        jLabel265.setFont(jLabel265.getFont().deriveFont(jLabel265.getFont().getSize()-5f));
        jLabel265.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel265.setText("28-21");
        jLabel265.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        add(jLabel265, new org.netbeans.lib.awtextra.AbsoluteConstraints(23, 322, 22, 22));

        jLabel266.setFont(jLabel266.getFont().deriveFont(jLabel266.getFont().getSize()-5f));
        jLabel266.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel266.setText("28-22");
        jLabel266.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        add(jLabel266, new org.netbeans.lib.awtextra.AbsoluteConstraints(46, 322, 22, 22));

        jLabel267.setFont(jLabel267.getFont().deriveFont(jLabel267.getFont().getSize()-5f));
        jLabel267.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel267.setText("28-23");
        jLabel267.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        add(jLabel267, new org.netbeans.lib.awtextra.AbsoluteConstraints(69, 322, 22, 22));

        jLabel268.setFont(jLabel268.getFont().deriveFont(jLabel268.getFont().getSize()-5f));
        jLabel268.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel268.setText("28-24");
        jLabel268.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        add(jLabel268, new org.netbeans.lib.awtextra.AbsoluteConstraints(92, 322, 22, 22));

        jLabel269.setFont(jLabel269.getFont().deriveFont(jLabel269.getFont().getSize()-5f));
        jLabel269.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel269.setText("28-25");
        jLabel269.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        add(jLabel269, new org.netbeans.lib.awtextra.AbsoluteConstraints(115, 322, 22, 22));

        jLabel270.setFont(jLabel270.getFont().deriveFont(jLabel270.getFont().getSize()-5f));
        jLabel270.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel270.setText("28-26");
        jLabel270.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        add(jLabel270, new org.netbeans.lib.awtextra.AbsoluteConstraints(138, 322, 22, 22));

        jLabel271.setFont(jLabel271.getFont().deriveFont(jLabel271.getFont().getSize()-5f));
        jLabel271.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel271.setText("28-27");
        jLabel271.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        add(jLabel271, new org.netbeans.lib.awtextra.AbsoluteConstraints(161, 322, 22, 22));

        jLabel272.setFont(jLabel272.getFont().deriveFont(jLabel272.getFont().getSize()-5f));
        jLabel272.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel272.setText("28-28");
        jLabel272.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        add(jLabel272, new org.netbeans.lib.awtextra.AbsoluteConstraints(184, 322, 22, 22));

        jLabel273.setFont(jLabel273.getFont().deriveFont(jLabel273.getFont().getSize()-5f));
        jLabel273.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel273.setText("28-29");
        jLabel273.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        add(jLabel273, new org.netbeans.lib.awtextra.AbsoluteConstraints(207, 322, 22, 22));

        jLabel274.setFont(jLabel274.getFont().deriveFont(jLabel274.getFont().getSize()-5f));
        jLabel274.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel274.setText("28-30");
        jLabel274.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        add(jLabel274, new org.netbeans.lib.awtextra.AbsoluteConstraints(230, 322, 22, 22));

        jLabel275.setFont(jLabel275.getFont().deriveFont(jLabel275.getFont().getSize()-5f));
        jLabel275.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel275.setText("28-31");
        jLabel275.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        add(jLabel275, new org.netbeans.lib.awtextra.AbsoluteConstraints(253, 322, 22, 22));

        jLabel276.setFont(jLabel276.getFont().deriveFont(jLabel276.getFont().getSize()-5f));
        jLabel276.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel276.setText("28-32");
        jLabel276.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        add(jLabel276, new org.netbeans.lib.awtextra.AbsoluteConstraints(276, 322, 22, 22));

        jLabel277.setFont(jLabel277.getFont().deriveFont(jLabel277.getFont().getSize()-5f));
        jLabel277.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel277.setText("28-33");
        jLabel277.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        add(jLabel277, new org.netbeans.lib.awtextra.AbsoluteConstraints(299, 322, 22, 22));

        jLabel278.setFont(jLabel278.getFont().deriveFont(jLabel278.getFont().getSize()-5f));
        jLabel278.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel278.setText("28-34");
        jLabel278.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        add(jLabel278, new org.netbeans.lib.awtextra.AbsoluteConstraints(322, 322, 22, 22));

        jLabel279.setFont(jLabel279.getFont().deriveFont(jLabel279.getFont().getSize()-5f));
        jLabel279.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel279.setText("28-35");
        jLabel279.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        add(jLabel279, new org.netbeans.lib.awtextra.AbsoluteConstraints(345, 322, 22, 22));

        jLabel280.setFont(jLabel280.getFont().deriveFont(jLabel280.getFont().getSize()-5f));
        jLabel280.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel280.setText("28-36");
        jLabel280.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        add(jLabel280, new org.netbeans.lib.awtextra.AbsoluteConstraints(368, 322, 22, 22));

        jLabel281.setFont(jLabel281.getFont().deriveFont(jLabel281.getFont().getSize()-5f));
        jLabel281.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel281.setText("28-37");
        jLabel281.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        add(jLabel281, new org.netbeans.lib.awtextra.AbsoluteConstraints(391, 322, 22, 22));

        jLabel282.setFont(jLabel282.getFont().deriveFont(jLabel282.getFont().getSize()-5f));
        jLabel282.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel282.setText("28-38");
        jLabel282.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        add(jLabel282, new org.netbeans.lib.awtextra.AbsoluteConstraints(414, 322, 22, 22));

        jLabel283.setFont(jLabel283.getFont().deriveFont(jLabel283.getFont().getSize()-5f));
        jLabel283.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel283.setText("28-39");
        jLabel283.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        add(jLabel283, new org.netbeans.lib.awtextra.AbsoluteConstraints(437, 322, 22, 22));

        jLabel284.setFont(jLabel284.getFont().deriveFont(jLabel284.getFont().getSize()-5f));
        jLabel284.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel284.setText("28-40");
        jLabel284.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        add(jLabel284, new org.netbeans.lib.awtextra.AbsoluteConstraints(460, 322, 22, 22));

        jLabel285.setFont(jLabel285.getFont().deriveFont(jLabel285.getFont().getSize()-5f));
        jLabel285.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel285.setText("28-41");
        jLabel285.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        add(jLabel285, new org.netbeans.lib.awtextra.AbsoluteConstraints(483, 322, 22, 22));

        jLabel286.setFont(jLabel286.getFont().deriveFont(jLabel286.getFont().getSize()-5f));
        jLabel286.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel286.setText("27-21");
        jLabel286.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        add(jLabel286, new org.netbeans.lib.awtextra.AbsoluteConstraints(23, 345, 22, 22));

        jLabel287.setFont(jLabel287.getFont().deriveFont(jLabel287.getFont().getSize()-5f));
        jLabel287.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel287.setText("27-22");
        jLabel287.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        add(jLabel287, new org.netbeans.lib.awtextra.AbsoluteConstraints(46, 345, 22, 22));

        jLabel288.setFont(jLabel288.getFont().deriveFont(jLabel288.getFont().getSize()-5f));
        jLabel288.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel288.setText("27-23");
        jLabel288.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        add(jLabel288, new org.netbeans.lib.awtextra.AbsoluteConstraints(69, 345, 22, 22));

        jLabel289.setFont(jLabel289.getFont().deriveFont(jLabel289.getFont().getSize()-5f));
        jLabel289.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel289.setText("27-24");
        jLabel289.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        add(jLabel289, new org.netbeans.lib.awtextra.AbsoluteConstraints(92, 345, 22, 22));

        jLabel290.setFont(jLabel290.getFont().deriveFont(jLabel290.getFont().getSize()-5f));
        jLabel290.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel290.setText("27-25");
        jLabel290.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        add(jLabel290, new org.netbeans.lib.awtextra.AbsoluteConstraints(115, 345, 22, 22));

        jLabel291.setFont(jLabel291.getFont().deriveFont(jLabel291.getFont().getSize()-5f));
        jLabel291.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel291.setText("27-26");
        jLabel291.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        add(jLabel291, new org.netbeans.lib.awtextra.AbsoluteConstraints(138, 345, 22, 22));

        jLabel292.setFont(jLabel292.getFont().deriveFont(jLabel292.getFont().getSize()-5f));
        jLabel292.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel292.setText("27-27");
        jLabel292.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        add(jLabel292, new org.netbeans.lib.awtextra.AbsoluteConstraints(161, 345, 22, 22));

        jLabel293.setFont(jLabel293.getFont().deriveFont(jLabel293.getFont().getSize()-5f));
        jLabel293.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel293.setText("27-28");
        jLabel293.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        add(jLabel293, new org.netbeans.lib.awtextra.AbsoluteConstraints(184, 345, 22, 22));

        jLabel294.setFont(jLabel294.getFont().deriveFont(jLabel294.getFont().getSize()-5f));
        jLabel294.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel294.setText("27-29");
        jLabel294.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        add(jLabel294, new org.netbeans.lib.awtextra.AbsoluteConstraints(207, 345, 22, 22));

        jLabel295.setFont(jLabel295.getFont().deriveFont(jLabel295.getFont().getSize()-5f));
        jLabel295.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel295.setText("27-30");
        jLabel295.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        add(jLabel295, new org.netbeans.lib.awtextra.AbsoluteConstraints(230, 345, 22, 22));

        jLabel296.setFont(jLabel296.getFont().deriveFont(jLabel296.getFont().getSize()-5f));
        jLabel296.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel296.setText("27-31");
        jLabel296.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        add(jLabel296, new org.netbeans.lib.awtextra.AbsoluteConstraints(253, 345, 22, 22));

        jLabel297.setFont(jLabel297.getFont().deriveFont(jLabel297.getFont().getSize()-5f));
        jLabel297.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel297.setText("27-32");
        jLabel297.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        add(jLabel297, new org.netbeans.lib.awtextra.AbsoluteConstraints(276, 345, 22, 22));

        jLabel298.setFont(jLabel298.getFont().deriveFont(jLabel298.getFont().getSize()-5f));
        jLabel298.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel298.setText("27-33");
        jLabel298.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        add(jLabel298, new org.netbeans.lib.awtextra.AbsoluteConstraints(299, 345, 22, 22));

        jLabel299.setFont(jLabel299.getFont().deriveFont(jLabel299.getFont().getSize()-5f));
        jLabel299.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel299.setText("27-34");
        jLabel299.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        add(jLabel299, new org.netbeans.lib.awtextra.AbsoluteConstraints(322, 345, 22, 22));

        jLabel300.setFont(jLabel300.getFont().deriveFont(jLabel300.getFont().getSize()-5f));
        jLabel300.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel300.setText("27-35");
        jLabel300.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        add(jLabel300, new org.netbeans.lib.awtextra.AbsoluteConstraints(345, 345, 22, 22));

        jLabel301.setFont(jLabel301.getFont().deriveFont(jLabel301.getFont().getSize()-5f));
        jLabel301.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel301.setText("27-36");
        jLabel301.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        add(jLabel301, new org.netbeans.lib.awtextra.AbsoluteConstraints(368, 345, 22, 22));

        jLabel302.setFont(jLabel302.getFont().deriveFont(jLabel302.getFont().getSize()-5f));
        jLabel302.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel302.setText("27-37");
        jLabel302.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        add(jLabel302, new org.netbeans.lib.awtextra.AbsoluteConstraints(391, 345, 22, 22));

        jLabel303.setFont(jLabel303.getFont().deriveFont(jLabel303.getFont().getSize()-5f));
        jLabel303.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel303.setText("27-38");
        jLabel303.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        add(jLabel303, new org.netbeans.lib.awtextra.AbsoluteConstraints(414, 345, 22, 22));

        jLabel304.setFont(jLabel304.getFont().deriveFont(jLabel304.getFont().getSize()-5f));
        jLabel304.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel304.setText("27-39");
        jLabel304.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        add(jLabel304, new org.netbeans.lib.awtextra.AbsoluteConstraints(437, 345, 22, 22));

        jLabel305.setFont(jLabel305.getFont().deriveFont(jLabel305.getFont().getSize()-5f));
        jLabel305.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel305.setText("27-40");
        jLabel305.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        add(jLabel305, new org.netbeans.lib.awtextra.AbsoluteConstraints(460, 345, 22, 22));

        jLabel306.setFont(jLabel306.getFont().deriveFont(jLabel306.getFont().getSize()-5f));
        jLabel306.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel306.setText("27-41");
        jLabel306.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        add(jLabel306, new org.netbeans.lib.awtextra.AbsoluteConstraints(483, 345, 22, 22));

        jLabel307.setFont(jLabel307.getFont().deriveFont(jLabel307.getFont().getSize()-5f));
        jLabel307.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel307.setText("26-21");
        jLabel307.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        add(jLabel307, new org.netbeans.lib.awtextra.AbsoluteConstraints(23, 368, 22, 22));

        jLabel308.setFont(jLabel308.getFont().deriveFont(jLabel308.getFont().getSize()-5f));
        jLabel308.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel308.setText("26-22");
        jLabel308.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        add(jLabel308, new org.netbeans.lib.awtextra.AbsoluteConstraints(46, 368, 22, 22));

        jLabel309.setFont(jLabel309.getFont().deriveFont(jLabel309.getFont().getSize()-5f));
        jLabel309.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel309.setText("26-23");
        jLabel309.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        add(jLabel309, new org.netbeans.lib.awtextra.AbsoluteConstraints(69, 368, 22, 22));

        jLabel310.setFont(jLabel310.getFont().deriveFont(jLabel310.getFont().getSize()-5f));
        jLabel310.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel310.setText("26-24");
        jLabel310.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        add(jLabel310, new org.netbeans.lib.awtextra.AbsoluteConstraints(92, 368, 22, 22));

        jLabel311.setFont(jLabel311.getFont().deriveFont(jLabel311.getFont().getSize()-5f));
        jLabel311.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel311.setText("26-25");
        jLabel311.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        add(jLabel311, new org.netbeans.lib.awtextra.AbsoluteConstraints(115, 368, 22, 22));

        jLabel312.setFont(jLabel312.getFont().deriveFont(jLabel312.getFont().getSize()-5f));
        jLabel312.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel312.setText("26-26");
        jLabel312.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        add(jLabel312, new org.netbeans.lib.awtextra.AbsoluteConstraints(138, 368, 22, 22));

        jLabel313.setFont(jLabel313.getFont().deriveFont(jLabel313.getFont().getSize()-5f));
        jLabel313.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel313.setText("26-27");
        jLabel313.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        add(jLabel313, new org.netbeans.lib.awtextra.AbsoluteConstraints(161, 368, 22, 22));

        jLabel314.setFont(jLabel314.getFont().deriveFont(jLabel314.getFont().getSize()-5f));
        jLabel314.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel314.setText("26-28");
        jLabel314.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        add(jLabel314, new org.netbeans.lib.awtextra.AbsoluteConstraints(184, 368, 22, 22));

        jLabel315.setFont(jLabel315.getFont().deriveFont(jLabel315.getFont().getSize()-5f));
        jLabel315.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel315.setText("26-29");
        jLabel315.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        add(jLabel315, new org.netbeans.lib.awtextra.AbsoluteConstraints(207, 368, 22, 22));

        jLabel316.setFont(jLabel316.getFont().deriveFont(jLabel316.getFont().getSize()-5f));
        jLabel316.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel316.setText("26-30");
        jLabel316.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        add(jLabel316, new org.netbeans.lib.awtextra.AbsoluteConstraints(230, 368, 22, 22));

        jLabel317.setFont(jLabel317.getFont().deriveFont(jLabel317.getFont().getSize()-5f));
        jLabel317.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel317.setText("26-31");
        jLabel317.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        add(jLabel317, new org.netbeans.lib.awtextra.AbsoluteConstraints(253, 368, 22, 22));

        jLabel318.setFont(jLabel318.getFont().deriveFont(jLabel318.getFont().getSize()-5f));
        jLabel318.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel318.setText("26-32");
        jLabel318.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        add(jLabel318, new org.netbeans.lib.awtextra.AbsoluteConstraints(276, 368, 22, 22));

        jLabel319.setFont(jLabel319.getFont().deriveFont(jLabel319.getFont().getSize()-5f));
        jLabel319.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel319.setText("26-33");
        jLabel319.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        add(jLabel319, new org.netbeans.lib.awtextra.AbsoluteConstraints(299, 368, 22, 22));

        jLabel320.setFont(jLabel320.getFont().deriveFont(jLabel320.getFont().getSize()-5f));
        jLabel320.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel320.setText("26-34");
        jLabel320.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        add(jLabel320, new org.netbeans.lib.awtextra.AbsoluteConstraints(322, 368, 22, 22));

        jLabel321.setFont(jLabel321.getFont().deriveFont(jLabel321.getFont().getSize()-5f));
        jLabel321.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel321.setText("26-35");
        jLabel321.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        add(jLabel321, new org.netbeans.lib.awtextra.AbsoluteConstraints(345, 368, 22, 22));

        jLabel322.setFont(jLabel322.getFont().deriveFont(jLabel322.getFont().getSize()-5f));
        jLabel322.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel322.setText("26-36");
        jLabel322.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        add(jLabel322, new org.netbeans.lib.awtextra.AbsoluteConstraints(368, 368, 22, 22));

        jLabel323.setFont(jLabel323.getFont().deriveFont(jLabel323.getFont().getSize()-5f));
        jLabel323.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel323.setText("26-37");
        jLabel323.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        add(jLabel323, new org.netbeans.lib.awtextra.AbsoluteConstraints(391, 368, 22, 22));

        jLabel324.setFont(jLabel324.getFont().deriveFont(jLabel324.getFont().getSize()-5f));
        jLabel324.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel324.setText("26-38");
        jLabel324.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        add(jLabel324, new org.netbeans.lib.awtextra.AbsoluteConstraints(414, 368, 22, 22));

        jLabel325.setFont(jLabel325.getFont().deriveFont(jLabel325.getFont().getSize()-5f));
        jLabel325.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel325.setText("26-39");
        jLabel325.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        add(jLabel325, new org.netbeans.lib.awtextra.AbsoluteConstraints(437, 368, 22, 22));

        jLabel326.setFont(jLabel326.getFont().deriveFont(jLabel326.getFont().getSize()-5f));
        jLabel326.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel326.setText("26-40");
        jLabel326.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        add(jLabel326, new org.netbeans.lib.awtextra.AbsoluteConstraints(460, 368, 22, 22));

        jLabel327.setFont(jLabel327.getFont().deriveFont(jLabel327.getFont().getSize()-5f));
        jLabel327.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel327.setText("26-41");
        jLabel327.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        add(jLabel327, new org.netbeans.lib.awtextra.AbsoluteConstraints(483, 368, 22, 22));

        jLabel328.setFont(jLabel328.getFont().deriveFont(jLabel328.getFont().getSize()-5f));
        jLabel328.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel328.setText("25-22");
        jLabel328.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        add(jLabel328, new org.netbeans.lib.awtextra.AbsoluteConstraints(46, 391, 22, 22));

        jLabel329.setFont(jLabel329.getFont().deriveFont(jLabel329.getFont().getSize()-5f));
        jLabel329.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel329.setText("25-23");
        jLabel329.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        add(jLabel329, new org.netbeans.lib.awtextra.AbsoluteConstraints(69, 391, 22, 22));

        jLabel330.setFont(jLabel330.getFont().deriveFont(jLabel330.getFont().getSize()-5f));
        jLabel330.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel330.setText("25-24");
        jLabel330.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        add(jLabel330, new org.netbeans.lib.awtextra.AbsoluteConstraints(92, 391, 22, 22));

        jLabel331.setFont(jLabel331.getFont().deriveFont(jLabel331.getFont().getSize()-5f));
        jLabel331.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel331.setText("25-25");
        jLabel331.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        add(jLabel331, new org.netbeans.lib.awtextra.AbsoluteConstraints(115, 391, 22, 22));

        jLabel332.setFont(jLabel332.getFont().deriveFont(jLabel332.getFont().getSize()-5f));
        jLabel332.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel332.setText("25-26");
        jLabel332.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        add(jLabel332, new org.netbeans.lib.awtextra.AbsoluteConstraints(138, 391, 22, 22));

        jLabel333.setFont(jLabel333.getFont().deriveFont(jLabel333.getFont().getSize()-5f));
        jLabel333.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel333.setText("25-27");
        jLabel333.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        add(jLabel333, new org.netbeans.lib.awtextra.AbsoluteConstraints(161, 391, 22, 22));

        jLabel334.setFont(jLabel334.getFont().deriveFont(jLabel334.getFont().getSize()-5f));
        jLabel334.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel334.setText("25-28");
        jLabel334.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        add(jLabel334, new org.netbeans.lib.awtextra.AbsoluteConstraints(184, 391, 22, 22));

        jLabel335.setFont(jLabel335.getFont().deriveFont(jLabel335.getFont().getSize()-5f));
        jLabel335.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel335.setText("25-29");
        jLabel335.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        add(jLabel335, new org.netbeans.lib.awtextra.AbsoluteConstraints(207, 391, 22, 22));

        jLabel336.setFont(jLabel336.getFont().deriveFont(jLabel336.getFont().getSize()-5f));
        jLabel336.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel336.setText("25-30");
        jLabel336.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        add(jLabel336, new org.netbeans.lib.awtextra.AbsoluteConstraints(230, 391, 22, 22));

        jLabel337.setFont(jLabel337.getFont().deriveFont(jLabel337.getFont().getSize()-5f));
        jLabel337.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel337.setText("25-31");
        jLabel337.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        add(jLabel337, new org.netbeans.lib.awtextra.AbsoluteConstraints(253, 391, 22, 22));

        jLabel338.setFont(jLabel338.getFont().deriveFont(jLabel338.getFont().getSize()-5f));
        jLabel338.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel338.setText("25-32");
        jLabel338.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        add(jLabel338, new org.netbeans.lib.awtextra.AbsoluteConstraints(276, 391, 22, 22));

        jLabel339.setFont(jLabel339.getFont().deriveFont(jLabel339.getFont().getSize()-5f));
        jLabel339.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel339.setText("25-33");
        jLabel339.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        add(jLabel339, new org.netbeans.lib.awtextra.AbsoluteConstraints(299, 391, 22, 22));

        jLabel340.setFont(jLabel340.getFont().deriveFont(jLabel340.getFont().getSize()-5f));
        jLabel340.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel340.setText("25-34");
        jLabel340.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        add(jLabel340, new org.netbeans.lib.awtextra.AbsoluteConstraints(322, 391, 22, 22));

        jLabel341.setFont(jLabel341.getFont().deriveFont(jLabel341.getFont().getSize()-5f));
        jLabel341.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel341.setText("25-35");
        jLabel341.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        add(jLabel341, new org.netbeans.lib.awtextra.AbsoluteConstraints(345, 391, 22, 22));

        jLabel342.setFont(jLabel342.getFont().deriveFont(jLabel342.getFont().getSize()-5f));
        jLabel342.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel342.setText("25-36");
        jLabel342.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        add(jLabel342, new org.netbeans.lib.awtextra.AbsoluteConstraints(368, 391, 22, 22));

        jLabel343.setFont(jLabel343.getFont().deriveFont(jLabel343.getFont().getSize()-5f));
        jLabel343.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel343.setText("25-37");
        jLabel343.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        add(jLabel343, new org.netbeans.lib.awtextra.AbsoluteConstraints(391, 391, 22, 22));

        jLabel344.setFont(jLabel344.getFont().deriveFont(jLabel344.getFont().getSize()-5f));
        jLabel344.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel344.setText("25-38");
        jLabel344.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        add(jLabel344, new org.netbeans.lib.awtextra.AbsoluteConstraints(414, 391, 22, 22));

        jLabel345.setFont(jLabel345.getFont().deriveFont(jLabel345.getFont().getSize()-5f));
        jLabel345.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel345.setText("25-39");
        jLabel345.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        add(jLabel345, new org.netbeans.lib.awtextra.AbsoluteConstraints(437, 391, 22, 22));

        jLabel346.setFont(jLabel346.getFont().deriveFont(jLabel346.getFont().getSize()-5f));
        jLabel346.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel346.setText("25-40");
        jLabel346.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        add(jLabel346, new org.netbeans.lib.awtextra.AbsoluteConstraints(460, 391, 22, 22));

        jLabel347.setFont(jLabel347.getFont().deriveFont(jLabel347.getFont().getSize()-5f));
        jLabel347.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel347.setText("24-22");
        jLabel347.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        add(jLabel347, new org.netbeans.lib.awtextra.AbsoluteConstraints(46, 414, 22, 22));

        jLabel348.setFont(jLabel348.getFont().deriveFont(jLabel348.getFont().getSize()-5f));
        jLabel348.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel348.setText("24-23");
        jLabel348.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        add(jLabel348, new org.netbeans.lib.awtextra.AbsoluteConstraints(69, 414, 22, 22));

        jLabel349.setFont(jLabel349.getFont().deriveFont(jLabel349.getFont().getSize()-5f));
        jLabel349.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel349.setText("24-24");
        jLabel349.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        add(jLabel349, new org.netbeans.lib.awtextra.AbsoluteConstraints(92, 414, 22, 22));

        jLabel350.setFont(jLabel350.getFont().deriveFont(jLabel350.getFont().getSize()-5f));
        jLabel350.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel350.setText("24-25");
        jLabel350.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        add(jLabel350, new org.netbeans.lib.awtextra.AbsoluteConstraints(115, 414, 22, 22));

        jLabel351.setFont(jLabel351.getFont().deriveFont(jLabel351.getFont().getSize()-5f));
        jLabel351.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel351.setText("24-26");
        jLabel351.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        add(jLabel351, new org.netbeans.lib.awtextra.AbsoluteConstraints(138, 414, 22, 22));

        jLabel352.setFont(jLabel352.getFont().deriveFont(jLabel352.getFont().getSize()-5f));
        jLabel352.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel352.setText("24-27");
        jLabel352.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        add(jLabel352, new org.netbeans.lib.awtextra.AbsoluteConstraints(161, 414, 22, 22));

        jLabel353.setFont(jLabel353.getFont().deriveFont(jLabel353.getFont().getSize()-5f));
        jLabel353.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel353.setText("24-28");
        jLabel353.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        add(jLabel353, new org.netbeans.lib.awtextra.AbsoluteConstraints(184, 414, 22, 22));

        jLabel354.setFont(jLabel354.getFont().deriveFont(jLabel354.getFont().getSize()-5f));
        jLabel354.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel354.setText("24-29");
        jLabel354.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        add(jLabel354, new org.netbeans.lib.awtextra.AbsoluteConstraints(207, 414, 22, 22));

        jLabel355.setFont(jLabel355.getFont().deriveFont(jLabel355.getFont().getSize()-5f));
        jLabel355.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel355.setText("24-30");
        jLabel355.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        add(jLabel355, new org.netbeans.lib.awtextra.AbsoluteConstraints(230, 414, 22, 22));

        jLabel356.setFont(jLabel356.getFont().deriveFont(jLabel356.getFont().getSize()-5f));
        jLabel356.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel356.setText("24-31");
        jLabel356.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        add(jLabel356, new org.netbeans.lib.awtextra.AbsoluteConstraints(253, 414, 22, 22));

        jLabel357.setFont(jLabel357.getFont().deriveFont(jLabel357.getFont().getSize()-5f));
        jLabel357.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel357.setText("24-32");
        jLabel357.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        add(jLabel357, new org.netbeans.lib.awtextra.AbsoluteConstraints(276, 414, 22, 22));

        jLabel358.setFont(jLabel358.getFont().deriveFont(jLabel358.getFont().getSize()-5f));
        jLabel358.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel358.setText("24-33");
        jLabel358.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        add(jLabel358, new org.netbeans.lib.awtextra.AbsoluteConstraints(299, 414, 22, 22));

        jLabel359.setFont(jLabel359.getFont().deriveFont(jLabel359.getFont().getSize()-5f));
        jLabel359.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel359.setText("24-34");
        jLabel359.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        add(jLabel359, new org.netbeans.lib.awtextra.AbsoluteConstraints(322, 414, 22, 22));

        jLabel360.setFont(jLabel360.getFont().deriveFont(jLabel360.getFont().getSize()-5f));
        jLabel360.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel360.setText("24-35");
        jLabel360.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        add(jLabel360, new org.netbeans.lib.awtextra.AbsoluteConstraints(345, 414, 22, 22));

        jLabel361.setFont(jLabel361.getFont().deriveFont(jLabel361.getFont().getSize()-5f));
        jLabel361.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel361.setText("24-36");
        jLabel361.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        add(jLabel361, new org.netbeans.lib.awtextra.AbsoluteConstraints(368, 414, 22, 22));

        jLabel362.setFont(jLabel362.getFont().deriveFont(jLabel362.getFont().getSize()-5f));
        jLabel362.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel362.setText("24-37");
        jLabel362.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        add(jLabel362, new org.netbeans.lib.awtextra.AbsoluteConstraints(391, 414, 22, 22));

        jLabel363.setFont(jLabel363.getFont().deriveFont(jLabel363.getFont().getSize()-5f));
        jLabel363.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel363.setText("24-38");
        jLabel363.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        add(jLabel363, new org.netbeans.lib.awtextra.AbsoluteConstraints(414, 414, 22, 22));

        jLabel364.setFont(jLabel364.getFont().deriveFont(jLabel364.getFont().getSize()-5f));
        jLabel364.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel364.setText("24-39");
        jLabel364.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        add(jLabel364, new org.netbeans.lib.awtextra.AbsoluteConstraints(437, 414, 22, 22));

        jLabel365.setFont(jLabel365.getFont().deriveFont(jLabel365.getFont().getSize()-5f));
        jLabel365.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel365.setText("24-40");
        jLabel365.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        add(jLabel365, new org.netbeans.lib.awtextra.AbsoluteConstraints(460, 414, 22, 22));

        jLabel366.setFont(jLabel366.getFont().deriveFont(jLabel366.getFont().getSize()-5f));
        jLabel366.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel366.setText("23-23");
        jLabel366.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        add(jLabel366, new org.netbeans.lib.awtextra.AbsoluteConstraints(69, 437, 22, 22));

        jLabel367.setFont(jLabel367.getFont().deriveFont(jLabel367.getFont().getSize()-5f));
        jLabel367.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel367.setText("23-24");
        jLabel367.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        add(jLabel367, new org.netbeans.lib.awtextra.AbsoluteConstraints(92, 437, 22, 22));

        jLabel368.setFont(jLabel368.getFont().deriveFont(jLabel368.getFont().getSize()-5f));
        jLabel368.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel368.setText("23-25");
        jLabel368.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        add(jLabel368, new org.netbeans.lib.awtextra.AbsoluteConstraints(115, 437, 22, 22));

        jLabel369.setFont(jLabel369.getFont().deriveFont(jLabel369.getFont().getSize()-5f));
        jLabel369.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel369.setText("23-26");
        jLabel369.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        add(jLabel369, new org.netbeans.lib.awtextra.AbsoluteConstraints(138, 437, 22, 22));

        jLabel370.setFont(jLabel370.getFont().deriveFont(jLabel370.getFont().getSize()-5f));
        jLabel370.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel370.setText("23-27");
        jLabel370.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        add(jLabel370, new org.netbeans.lib.awtextra.AbsoluteConstraints(161, 437, 22, 22));

        jLabel371.setFont(jLabel371.getFont().deriveFont(jLabel371.getFont().getSize()-5f));
        jLabel371.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel371.setText("23-28");
        jLabel371.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        add(jLabel371, new org.netbeans.lib.awtextra.AbsoluteConstraints(184, 437, 22, 22));

        jLabel372.setFont(jLabel372.getFont().deriveFont(jLabel372.getFont().getSize()-5f));
        jLabel372.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel372.setText("23-29");
        jLabel372.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        add(jLabel372, new org.netbeans.lib.awtextra.AbsoluteConstraints(207, 437, 22, 22));

        jLabel373.setFont(jLabel373.getFont().deriveFont(jLabel373.getFont().getSize()-5f));
        jLabel373.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel373.setText("23-30");
        jLabel373.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        add(jLabel373, new org.netbeans.lib.awtextra.AbsoluteConstraints(230, 437, 22, 22));

        jLabel374.setFont(jLabel374.getFont().deriveFont(jLabel374.getFont().getSize()-5f));
        jLabel374.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel374.setText("23-31");
        jLabel374.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        add(jLabel374, new org.netbeans.lib.awtextra.AbsoluteConstraints(253, 437, 22, 22));

        jLabel375.setFont(jLabel375.getFont().deriveFont(jLabel375.getFont().getSize()-5f));
        jLabel375.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel375.setText("23-32");
        jLabel375.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        add(jLabel375, new org.netbeans.lib.awtextra.AbsoluteConstraints(276, 437, 22, 22));

        jLabel376.setFont(jLabel376.getFont().deriveFont(jLabel376.getFont().getSize()-5f));
        jLabel376.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel376.setText("23-33");
        jLabel376.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        add(jLabel376, new org.netbeans.lib.awtextra.AbsoluteConstraints(299, 437, 22, 22));

        jLabel377.setFont(jLabel377.getFont().deriveFont(jLabel377.getFont().getSize()-5f));
        jLabel377.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel377.setText("23-34");
        jLabel377.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        add(jLabel377, new org.netbeans.lib.awtextra.AbsoluteConstraints(322, 437, 22, 22));

        jLabel378.setFont(jLabel378.getFont().deriveFont(jLabel378.getFont().getSize()-5f));
        jLabel378.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel378.setText("23-35");
        jLabel378.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        add(jLabel378, new org.netbeans.lib.awtextra.AbsoluteConstraints(345, 437, 22, 22));

        jLabel379.setFont(jLabel379.getFont().deriveFont(jLabel379.getFont().getSize()-5f));
        jLabel379.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel379.setText("23-36");
        jLabel379.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        add(jLabel379, new org.netbeans.lib.awtextra.AbsoluteConstraints(368, 437, 22, 22));

        jLabel380.setFont(jLabel380.getFont().deriveFont(jLabel380.getFont().getSize()-5f));
        jLabel380.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel380.setText("23-37");
        jLabel380.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        add(jLabel380, new org.netbeans.lib.awtextra.AbsoluteConstraints(391, 437, 22, 22));

        jLabel381.setFont(jLabel381.getFont().deriveFont(jLabel381.getFont().getSize()-5f));
        jLabel381.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel381.setText("23-38");
        jLabel381.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        add(jLabel381, new org.netbeans.lib.awtextra.AbsoluteConstraints(414, 437, 22, 22));

        jLabel382.setFont(jLabel382.getFont().deriveFont(jLabel382.getFont().getSize()-5f));
        jLabel382.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel382.setText("23-39");
        jLabel382.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        add(jLabel382, new org.netbeans.lib.awtextra.AbsoluteConstraints(437, 437, 22, 22));

        jLabel383.setFont(jLabel383.getFont().deriveFont(jLabel383.getFont().getSize()-5f));
        jLabel383.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel383.setText("22-24");
        jLabel383.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        add(jLabel383, new org.netbeans.lib.awtextra.AbsoluteConstraints(92, 460, 22, 22));

        jLabel384.setFont(jLabel384.getFont().deriveFont(jLabel384.getFont().getSize()-5f));
        jLabel384.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel384.setText("22-25");
        jLabel384.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        add(jLabel384, new org.netbeans.lib.awtextra.AbsoluteConstraints(115, 460, 22, 22));

        jLabel385.setFont(jLabel385.getFont().deriveFont(jLabel385.getFont().getSize()-5f));
        jLabel385.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel385.setText("22-26");
        jLabel385.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        add(jLabel385, new org.netbeans.lib.awtextra.AbsoluteConstraints(138, 460, 22, 22));

        jLabel386.setFont(jLabel386.getFont().deriveFont(jLabel386.getFont().getSize()-5f));
        jLabel386.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel386.setText("22-27");
        jLabel386.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        add(jLabel386, new org.netbeans.lib.awtextra.AbsoluteConstraints(161, 460, 22, 22));

        jLabel387.setFont(jLabel387.getFont().deriveFont(jLabel387.getFont().getSize()-5f));
        jLabel387.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel387.setText("22-28");
        jLabel387.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        add(jLabel387, new org.netbeans.lib.awtextra.AbsoluteConstraints(184, 460, 22, 22));

        jLabel388.setFont(jLabel388.getFont().deriveFont(jLabel388.getFont().getSize()-5f));
        jLabel388.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel388.setText("22-29");
        jLabel388.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        add(jLabel388, new org.netbeans.lib.awtextra.AbsoluteConstraints(207, 460, 22, 22));

        jLabel389.setFont(jLabel389.getFont().deriveFont(jLabel389.getFont().getSize()-5f));
        jLabel389.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel389.setText("22-30");
        jLabel389.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        add(jLabel389, new org.netbeans.lib.awtextra.AbsoluteConstraints(230, 460, 22, 22));

        jLabel390.setFont(jLabel390.getFont().deriveFont(jLabel390.getFont().getSize()-5f));
        jLabel390.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel390.setText("22-31");
        jLabel390.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        add(jLabel390, new org.netbeans.lib.awtextra.AbsoluteConstraints(253, 460, 22, 22));

        jLabel391.setFont(jLabel391.getFont().deriveFont(jLabel391.getFont().getSize()-5f));
        jLabel391.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel391.setText("22-32");
        jLabel391.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        add(jLabel391, new org.netbeans.lib.awtextra.AbsoluteConstraints(276, 460, 22, 22));

        jLabel392.setFont(jLabel392.getFont().deriveFont(jLabel392.getFont().getSize()-5f));
        jLabel392.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel392.setText("22-33");
        jLabel392.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        add(jLabel392, new org.netbeans.lib.awtextra.AbsoluteConstraints(299, 460, 22, 22));

        jLabel393.setFont(jLabel393.getFont().deriveFont(jLabel393.getFont().getSize()-5f));
        jLabel393.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel393.setText("22-34");
        jLabel393.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        add(jLabel393, new org.netbeans.lib.awtextra.AbsoluteConstraints(322, 460, 22, 22));

        jLabel394.setFont(jLabel394.getFont().deriveFont(jLabel394.getFont().getSize()-5f));
        jLabel394.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel394.setText("22-35");
        jLabel394.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        add(jLabel394, new org.netbeans.lib.awtextra.AbsoluteConstraints(345, 460, 22, 22));

        jLabel395.setFont(jLabel395.getFont().deriveFont(jLabel395.getFont().getSize()-5f));
        jLabel395.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel395.setText("22-36");
        jLabel395.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        add(jLabel395, new org.netbeans.lib.awtextra.AbsoluteConstraints(368, 460, 22, 22));

        jLabel396.setFont(jLabel396.getFont().deriveFont(jLabel396.getFont().getSize()-5f));
        jLabel396.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel396.setText("22-37");
        jLabel396.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        add(jLabel396, new org.netbeans.lib.awtextra.AbsoluteConstraints(391, 460, 22, 22));

        jLabel397.setFont(jLabel397.getFont().deriveFont(jLabel397.getFont().getSize()-5f));
        jLabel397.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel397.setText("22-38");
        jLabel397.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        add(jLabel397, new org.netbeans.lib.awtextra.AbsoluteConstraints(414, 460, 22, 22));

        jLabel398.setFont(jLabel398.getFont().deriveFont(jLabel398.getFont().getSize()-5f));
        jLabel398.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel398.setText("21-26");
        jLabel398.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        add(jLabel398, new org.netbeans.lib.awtextra.AbsoluteConstraints(138, 483, 22, 22));

        jLabel399.setFont(jLabel399.getFont().deriveFont(jLabel399.getFont().getSize()-5f));
        jLabel399.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel399.setText("21-27");
        jLabel399.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        add(jLabel399, new org.netbeans.lib.awtextra.AbsoluteConstraints(161, 483, 22, 22));

        jLabel400.setFont(jLabel400.getFont().deriveFont(jLabel400.getFont().getSize()-5f));
        jLabel400.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel400.setText("21-28");
        jLabel400.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        add(jLabel400, new org.netbeans.lib.awtextra.AbsoluteConstraints(184, 483, 22, 22));

        jLabel401.setFont(jLabel401.getFont().deriveFont(jLabel401.getFont().getSize()-5f));
        jLabel401.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel401.setText("21-29");
        jLabel401.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        add(jLabel401, new org.netbeans.lib.awtextra.AbsoluteConstraints(207, 483, 22, 22));

        jLabel402.setFont(jLabel402.getFont().deriveFont(jLabel402.getFont().getSize()-5f));
        jLabel402.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel402.setText("21-30");
        jLabel402.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        add(jLabel402, new org.netbeans.lib.awtextra.AbsoluteConstraints(230, 483, 22, 22));

        jLabel403.setFont(jLabel403.getFont().deriveFont(jLabel403.getFont().getSize()-5f));
        jLabel403.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel403.setText("21-31");
        jLabel403.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        add(jLabel403, new org.netbeans.lib.awtextra.AbsoluteConstraints(253, 483, 22, 22));

        jLabel404.setFont(jLabel404.getFont().deriveFont(jLabel404.getFont().getSize()-5f));
        jLabel404.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel404.setText("21-32");
        jLabel404.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        add(jLabel404, new org.netbeans.lib.awtextra.AbsoluteConstraints(276, 483, 22, 22));

        jLabel405.setFont(jLabel405.getFont().deriveFont(jLabel405.getFont().getSize()-5f));
        jLabel405.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel405.setText("21-33");
        jLabel405.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        add(jLabel405, new org.netbeans.lib.awtextra.AbsoluteConstraints(299, 483, 22, 22));

        jLabel406.setFont(jLabel406.getFont().deriveFont(jLabel406.getFont().getSize()-5f));
        jLabel406.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel406.setText("21-34");
        jLabel406.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        add(jLabel406, new org.netbeans.lib.awtextra.AbsoluteConstraints(322, 483, 22, 22));

        jLabel407.setFont(jLabel407.getFont().deriveFont(jLabel407.getFont().getSize()-5f));
        jLabel407.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel407.setText("21-35");
        jLabel407.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        add(jLabel407, new org.netbeans.lib.awtextra.AbsoluteConstraints(345, 483, 22, 22));

        jLabel408.setFont(jLabel408.getFont().deriveFont(jLabel408.getFont().getSize()-5f));
        jLabel408.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel408.setText("21-36");
        jLabel408.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        add(jLabel408, new org.netbeans.lib.awtextra.AbsoluteConstraints(368, 483, 22, 22));

        jLabel409.setFont(jLabel409.getFont().deriveFont(jLabel409.getFont().getSize()-5f));
        jLabel409.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel409.setText("20-29");
        jLabel409.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        add(jLabel409, new org.netbeans.lib.awtextra.AbsoluteConstraints(207, 506, 22, 22));

        jLabel410.setFont(jLabel410.getFont().deriveFont(jLabel410.getFont().getSize()-5f));
        jLabel410.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel410.setText("20-30");
        jLabel410.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        add(jLabel410, new org.netbeans.lib.awtextra.AbsoluteConstraints(230, 506, 22, 22));

        jLabel411.setFont(jLabel411.getFont().deriveFont(jLabel411.getFont().getSize()-5f));
        jLabel411.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel411.setText("20-31");
        jLabel411.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        add(jLabel411, new org.netbeans.lib.awtextra.AbsoluteConstraints(253, 506, 22, 22));

        jLabel412.setFont(jLabel412.getFont().deriveFont(jLabel412.getFont().getSize()-5f));
        jLabel412.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel412.setText("20-32");
        jLabel412.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        add(jLabel412, new org.netbeans.lib.awtextra.AbsoluteConstraints(276, 506, 22, 22));

        jLabel413.setFont(jLabel413.getFont().deriveFont(jLabel413.getFont().getSize()-5f));
        jLabel413.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel413.setText("20-33");
        jLabel413.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        add(jLabel413, new org.netbeans.lib.awtextra.AbsoluteConstraints(299, 506, 22, 22));
    }// </editor-fold>//GEN-END:initComponents

    // <editor-fold defaultstate="collapsed" desc="Generated variables declatation">
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel100;
    private javax.swing.JLabel jLabel101;
    private javax.swing.JLabel jLabel102;
    private javax.swing.JLabel jLabel103;
    private javax.swing.JLabel jLabel104;
    private javax.swing.JLabel jLabel105;
    private javax.swing.JLabel jLabel106;
    private javax.swing.JLabel jLabel107;
    private javax.swing.JLabel jLabel108;
    private javax.swing.JLabel jLabel109;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel110;
    private javax.swing.JLabel jLabel111;
    private javax.swing.JLabel jLabel112;
    private javax.swing.JLabel jLabel113;
    private javax.swing.JLabel jLabel114;
    private javax.swing.JLabel jLabel115;
    private javax.swing.JLabel jLabel116;
    private javax.swing.JLabel jLabel117;
    private javax.swing.JLabel jLabel118;
    private javax.swing.JLabel jLabel119;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel120;
    private javax.swing.JLabel jLabel121;
    private javax.swing.JLabel jLabel122;
    private javax.swing.JLabel jLabel123;
    private javax.swing.JLabel jLabel124;
    private javax.swing.JLabel jLabel125;
    private javax.swing.JLabel jLabel126;
    private javax.swing.JLabel jLabel127;
    private javax.swing.JLabel jLabel128;
    private javax.swing.JLabel jLabel129;
    private javax.swing.JLabel jLabel13;
    private javax.swing.JLabel jLabel130;
    private javax.swing.JLabel jLabel131;
    private javax.swing.JLabel jLabel132;
    private javax.swing.JLabel jLabel133;
    private javax.swing.JLabel jLabel134;
    private javax.swing.JLabel jLabel135;
    private javax.swing.JLabel jLabel136;
    private javax.swing.JLabel jLabel137;
    private javax.swing.JLabel jLabel138;
    private javax.swing.JLabel jLabel139;
    private javax.swing.JLabel jLabel14;
    private javax.swing.JLabel jLabel140;
    private javax.swing.JLabel jLabel141;
    private javax.swing.JLabel jLabel142;
    private javax.swing.JLabel jLabel143;
    private javax.swing.JLabel jLabel144;
    private javax.swing.JLabel jLabel145;
    private javax.swing.JLabel jLabel146;
    private javax.swing.JLabel jLabel147;
    private javax.swing.JLabel jLabel148;
    private javax.swing.JLabel jLabel149;
    private javax.swing.JLabel jLabel15;
    private javax.swing.JLabel jLabel150;
    private javax.swing.JLabel jLabel151;
    private javax.swing.JLabel jLabel152;
    private javax.swing.JLabel jLabel153;
    private javax.swing.JLabel jLabel154;
    private javax.swing.JLabel jLabel155;
    private javax.swing.JLabel jLabel156;
    private javax.swing.JLabel jLabel157;
    private javax.swing.JLabel jLabel158;
    private javax.swing.JLabel jLabel159;
    private javax.swing.JLabel jLabel16;
    private javax.swing.JLabel jLabel160;
    private javax.swing.JLabel jLabel161;
    private javax.swing.JLabel jLabel162;
    private javax.swing.JLabel jLabel163;
    private javax.swing.JLabel jLabel164;
    private javax.swing.JLabel jLabel165;
    private javax.swing.JLabel jLabel166;
    private javax.swing.JLabel jLabel167;
    private javax.swing.JLabel jLabel168;
    private javax.swing.JLabel jLabel169;
    private javax.swing.JLabel jLabel17;
    private javax.swing.JLabel jLabel170;
    private javax.swing.JLabel jLabel171;
    private javax.swing.JLabel jLabel172;
    private javax.swing.JLabel jLabel173;
    private javax.swing.JLabel jLabel174;
    private javax.swing.JLabel jLabel175;
    private javax.swing.JLabel jLabel176;
    private javax.swing.JLabel jLabel177;
    private javax.swing.JLabel jLabel178;
    private javax.swing.JLabel jLabel179;
    private javax.swing.JLabel jLabel18;
    private javax.swing.JLabel jLabel180;
    private javax.swing.JLabel jLabel181;
    private javax.swing.JLabel jLabel182;
    private javax.swing.JLabel jLabel183;
    private javax.swing.JLabel jLabel184;
    private javax.swing.JLabel jLabel185;
    private javax.swing.JLabel jLabel186;
    private javax.swing.JLabel jLabel187;
    private javax.swing.JLabel jLabel188;
    private javax.swing.JLabel jLabel189;
    private javax.swing.JLabel jLabel19;
    private javax.swing.JLabel jLabel190;
    private javax.swing.JLabel jLabel191;
    private javax.swing.JLabel jLabel192;
    private javax.swing.JLabel jLabel193;
    private javax.swing.JLabel jLabel194;
    private javax.swing.JLabel jLabel195;
    private javax.swing.JLabel jLabel196;
    private javax.swing.JLabel jLabel197;
    private javax.swing.JLabel jLabel198;
    private javax.swing.JLabel jLabel199;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel20;
    private javax.swing.JLabel jLabel200;
    private javax.swing.JLabel jLabel201;
    private javax.swing.JLabel jLabel202;
    private javax.swing.JLabel jLabel203;
    private javax.swing.JLabel jLabel204;
    private javax.swing.JLabel jLabel205;
    private javax.swing.JLabel jLabel206;
    private javax.swing.JLabel jLabel207;
    private javax.swing.JLabel jLabel208;
    private javax.swing.JLabel jLabel209;
    private javax.swing.JLabel jLabel21;
    private javax.swing.JLabel jLabel210;
    private javax.swing.JLabel jLabel211;
    private javax.swing.JLabel jLabel212;
    private javax.swing.JLabel jLabel213;
    private javax.swing.JLabel jLabel214;
    private javax.swing.JLabel jLabel215;
    private javax.swing.JLabel jLabel216;
    private javax.swing.JLabel jLabel217;
    private javax.swing.JLabel jLabel218;
    private javax.swing.JLabel jLabel219;
    private javax.swing.JLabel jLabel22;
    private javax.swing.JLabel jLabel220;
    private javax.swing.JLabel jLabel221;
    private javax.swing.JLabel jLabel222;
    private javax.swing.JLabel jLabel223;
    private javax.swing.JLabel jLabel224;
    private javax.swing.JLabel jLabel225;
    private javax.swing.JLabel jLabel226;
    private javax.swing.JLabel jLabel227;
    private javax.swing.JLabel jLabel228;
    private javax.swing.JLabel jLabel229;
    private javax.swing.JLabel jLabel23;
    private javax.swing.JLabel jLabel230;
    private javax.swing.JLabel jLabel231;
    private javax.swing.JLabel jLabel232;
    private javax.swing.JLabel jLabel233;
    private javax.swing.JLabel jLabel234;
    private javax.swing.JLabel jLabel235;
    private javax.swing.JLabel jLabel236;
    private javax.swing.JLabel jLabel237;
    private javax.swing.JLabel jLabel238;
    private javax.swing.JLabel jLabel239;
    private javax.swing.JLabel jLabel24;
    private javax.swing.JLabel jLabel240;
    private javax.swing.JLabel jLabel241;
    private javax.swing.JLabel jLabel242;
    private javax.swing.JLabel jLabel243;
    private javax.swing.JLabel jLabel244;
    private javax.swing.JLabel jLabel245;
    private javax.swing.JLabel jLabel246;
    private javax.swing.JLabel jLabel247;
    private javax.swing.JLabel jLabel248;
    private javax.swing.JLabel jLabel249;
    private javax.swing.JLabel jLabel25;
    private javax.swing.JLabel jLabel250;
    private javax.swing.JLabel jLabel251;
    private javax.swing.JLabel jLabel252;
    private javax.swing.JLabel jLabel253;
    private javax.swing.JLabel jLabel254;
    private javax.swing.JLabel jLabel255;
    private javax.swing.JLabel jLabel256;
    private javax.swing.JLabel jLabel257;
    private javax.swing.JLabel jLabel258;
    private javax.swing.JLabel jLabel259;
    private javax.swing.JLabel jLabel26;
    private javax.swing.JLabel jLabel260;
    private javax.swing.JLabel jLabel261;
    private javax.swing.JLabel jLabel262;
    private javax.swing.JLabel jLabel263;
    private javax.swing.JLabel jLabel264;
    private javax.swing.JLabel jLabel265;
    private javax.swing.JLabel jLabel266;
    private javax.swing.JLabel jLabel267;
    private javax.swing.JLabel jLabel268;
    private javax.swing.JLabel jLabel269;
    private javax.swing.JLabel jLabel27;
    private javax.swing.JLabel jLabel270;
    private javax.swing.JLabel jLabel271;
    private javax.swing.JLabel jLabel272;
    private javax.swing.JLabel jLabel273;
    private javax.swing.JLabel jLabel274;
    private javax.swing.JLabel jLabel275;
    private javax.swing.JLabel jLabel276;
    private javax.swing.JLabel jLabel277;
    private javax.swing.JLabel jLabel278;
    private javax.swing.JLabel jLabel279;
    private javax.swing.JLabel jLabel28;
    private javax.swing.JLabel jLabel280;
    private javax.swing.JLabel jLabel281;
    private javax.swing.JLabel jLabel282;
    private javax.swing.JLabel jLabel283;
    private javax.swing.JLabel jLabel284;
    private javax.swing.JLabel jLabel285;
    private javax.swing.JLabel jLabel286;
    private javax.swing.JLabel jLabel287;
    private javax.swing.JLabel jLabel288;
    private javax.swing.JLabel jLabel289;
    private javax.swing.JLabel jLabel29;
    private javax.swing.JLabel jLabel290;
    private javax.swing.JLabel jLabel291;
    private javax.swing.JLabel jLabel292;
    private javax.swing.JLabel jLabel293;
    private javax.swing.JLabel jLabel294;
    private javax.swing.JLabel jLabel295;
    private javax.swing.JLabel jLabel296;
    private javax.swing.JLabel jLabel297;
    private javax.swing.JLabel jLabel298;
    private javax.swing.JLabel jLabel299;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel30;
    private javax.swing.JLabel jLabel300;
    private javax.swing.JLabel jLabel301;
    private javax.swing.JLabel jLabel302;
    private javax.swing.JLabel jLabel303;
    private javax.swing.JLabel jLabel304;
    private javax.swing.JLabel jLabel305;
    private javax.swing.JLabel jLabel306;
    private javax.swing.JLabel jLabel307;
    private javax.swing.JLabel jLabel308;
    private javax.swing.JLabel jLabel309;
    private javax.swing.JLabel jLabel31;
    private javax.swing.JLabel jLabel310;
    private javax.swing.JLabel jLabel311;
    private javax.swing.JLabel jLabel312;
    private javax.swing.JLabel jLabel313;
    private javax.swing.JLabel jLabel314;
    private javax.swing.JLabel jLabel315;
    private javax.swing.JLabel jLabel316;
    private javax.swing.JLabel jLabel317;
    private javax.swing.JLabel jLabel318;
    private javax.swing.JLabel jLabel319;
    private javax.swing.JLabel jLabel32;
    private javax.swing.JLabel jLabel320;
    private javax.swing.JLabel jLabel321;
    private javax.swing.JLabel jLabel322;
    private javax.swing.JLabel jLabel323;
    private javax.swing.JLabel jLabel324;
    private javax.swing.JLabel jLabel325;
    private javax.swing.JLabel jLabel326;
    private javax.swing.JLabel jLabel327;
    private javax.swing.JLabel jLabel328;
    private javax.swing.JLabel jLabel329;
    private javax.swing.JLabel jLabel33;
    private javax.swing.JLabel jLabel330;
    private javax.swing.JLabel jLabel331;
    private javax.swing.JLabel jLabel332;
    private javax.swing.JLabel jLabel333;
    private javax.swing.JLabel jLabel334;
    private javax.swing.JLabel jLabel335;
    private javax.swing.JLabel jLabel336;
    private javax.swing.JLabel jLabel337;
    private javax.swing.JLabel jLabel338;
    private javax.swing.JLabel jLabel339;
    private javax.swing.JLabel jLabel34;
    private javax.swing.JLabel jLabel340;
    private javax.swing.JLabel jLabel341;
    private javax.swing.JLabel jLabel342;
    private javax.swing.JLabel jLabel343;
    private javax.swing.JLabel jLabel344;
    private javax.swing.JLabel jLabel345;
    private javax.swing.JLabel jLabel346;
    private javax.swing.JLabel jLabel347;
    private javax.swing.JLabel jLabel348;
    private javax.swing.JLabel jLabel349;
    private javax.swing.JLabel jLabel35;
    private javax.swing.JLabel jLabel350;
    private javax.swing.JLabel jLabel351;
    private javax.swing.JLabel jLabel352;
    private javax.swing.JLabel jLabel353;
    private javax.swing.JLabel jLabel354;
    private javax.swing.JLabel jLabel355;
    private javax.swing.JLabel jLabel356;
    private javax.swing.JLabel jLabel357;
    private javax.swing.JLabel jLabel358;
    private javax.swing.JLabel jLabel359;
    private javax.swing.JLabel jLabel36;
    private javax.swing.JLabel jLabel360;
    private javax.swing.JLabel jLabel361;
    private javax.swing.JLabel jLabel362;
    private javax.swing.JLabel jLabel363;
    private javax.swing.JLabel jLabel364;
    private javax.swing.JLabel jLabel365;
    private javax.swing.JLabel jLabel366;
    private javax.swing.JLabel jLabel367;
    private javax.swing.JLabel jLabel368;
    private javax.swing.JLabel jLabel369;
    private javax.swing.JLabel jLabel37;
    private javax.swing.JLabel jLabel370;
    private javax.swing.JLabel jLabel371;
    private javax.swing.JLabel jLabel372;
    private javax.swing.JLabel jLabel373;
    private javax.swing.JLabel jLabel374;
    private javax.swing.JLabel jLabel375;
    private javax.swing.JLabel jLabel376;
    private javax.swing.JLabel jLabel377;
    private javax.swing.JLabel jLabel378;
    private javax.swing.JLabel jLabel379;
    private javax.swing.JLabel jLabel38;
    private javax.swing.JLabel jLabel380;
    private javax.swing.JLabel jLabel381;
    private javax.swing.JLabel jLabel382;
    private javax.swing.JLabel jLabel383;
    private javax.swing.JLabel jLabel384;
    private javax.swing.JLabel jLabel385;
    private javax.swing.JLabel jLabel386;
    private javax.swing.JLabel jLabel387;
    private javax.swing.JLabel jLabel388;
    private javax.swing.JLabel jLabel389;
    private javax.swing.JLabel jLabel39;
    private javax.swing.JLabel jLabel390;
    private javax.swing.JLabel jLabel391;
    private javax.swing.JLabel jLabel392;
    private javax.swing.JLabel jLabel393;
    private javax.swing.JLabel jLabel394;
    private javax.swing.JLabel jLabel395;
    private javax.swing.JLabel jLabel396;
    private javax.swing.JLabel jLabel397;
    private javax.swing.JLabel jLabel398;
    private javax.swing.JLabel jLabel399;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel40;
    private javax.swing.JLabel jLabel400;
    private javax.swing.JLabel jLabel401;
    private javax.swing.JLabel jLabel402;
    private javax.swing.JLabel jLabel403;
    private javax.swing.JLabel jLabel404;
    private javax.swing.JLabel jLabel405;
    private javax.swing.JLabel jLabel406;
    private javax.swing.JLabel jLabel407;
    private javax.swing.JLabel jLabel408;
    private javax.swing.JLabel jLabel409;
    private javax.swing.JLabel jLabel41;
    private javax.swing.JLabel jLabel410;
    private javax.swing.JLabel jLabel411;
    private javax.swing.JLabel jLabel412;
    private javax.swing.JLabel jLabel413;
    private javax.swing.JLabel jLabel42;
    private javax.swing.JLabel jLabel43;
    private javax.swing.JLabel jLabel44;
    private javax.swing.JLabel jLabel45;
    private javax.swing.JLabel jLabel46;
    private javax.swing.JLabel jLabel47;
    private javax.swing.JLabel jLabel48;
    private javax.swing.JLabel jLabel49;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel50;
    private javax.swing.JLabel jLabel51;
    private javax.swing.JLabel jLabel52;
    private javax.swing.JLabel jLabel53;
    private javax.swing.JLabel jLabel54;
    private javax.swing.JLabel jLabel55;
    private javax.swing.JLabel jLabel56;
    private javax.swing.JLabel jLabel57;
    private javax.swing.JLabel jLabel58;
    private javax.swing.JLabel jLabel59;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel60;
    private javax.swing.JLabel jLabel61;
    private javax.swing.JLabel jLabel62;
    private javax.swing.JLabel jLabel63;
    private javax.swing.JLabel jLabel64;
    private javax.swing.JLabel jLabel65;
    private javax.swing.JLabel jLabel66;
    private javax.swing.JLabel jLabel67;
    private javax.swing.JLabel jLabel68;
    private javax.swing.JLabel jLabel69;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel70;
    private javax.swing.JLabel jLabel71;
    private javax.swing.JLabel jLabel72;
    private javax.swing.JLabel jLabel73;
    private javax.swing.JLabel jLabel74;
    private javax.swing.JLabel jLabel75;
    private javax.swing.JLabel jLabel76;
    private javax.swing.JLabel jLabel77;
    private javax.swing.JLabel jLabel78;
    private javax.swing.JLabel jLabel79;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel80;
    private javax.swing.JLabel jLabel81;
    private javax.swing.JLabel jLabel82;
    private javax.swing.JLabel jLabel83;
    private javax.swing.JLabel jLabel84;
    private javax.swing.JLabel jLabel85;
    private javax.swing.JLabel jLabel86;
    private javax.swing.JLabel jLabel87;
    private javax.swing.JLabel jLabel88;
    private javax.swing.JLabel jLabel89;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JLabel jLabel90;
    private javax.swing.JLabel jLabel91;
    private javax.swing.JLabel jLabel92;
    private javax.swing.JLabel jLabel93;
    private javax.swing.JLabel jLabel94;
    private javax.swing.JLabel jLabel95;
    private javax.swing.JLabel jLabel96;
    private javax.swing.JLabel jLabel97;
    private javax.swing.JLabel jLabel98;
    private javax.swing.JLabel jLabel99;
    // End of variables declaration//GEN-END:variables
    // </editor-fold>
}
