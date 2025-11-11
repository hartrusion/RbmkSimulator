# Contributing

This project is in a very early stage of development and lots of 
architecture and design decisions are non-final. There is still a lot of 
refactoring and things might turn out not to work as desired.

The best thing to do is **not** to contribute code in this early stage.

1. There are no low-hanging fruits. A lot of things are not implemented as 
   there is no proper scalable architecture.
2. A lot of modeling is done that way for a reason (keeping it possible to 
   calculate even in extreme conditions is one such thing).
3. No external dependencies. This is a personal thing, I do not like this 
   project to be a blackbox mess of libraries. Yes, there are great charts that 
   are way better than the used jmplot package.
4. Code must be easy to read and understand. A lot of class fields might be 
   unnecessary and can be considered obsolete, however, sometimes they also 
   serve as a description of what is actually going on or JavaDoc is used on 
   class fields.

Please **do not create pull requests** as those are very likely to be denied 
in that early stage without prior discussion. 

