[![Scala.js](https://www.scala-js.org/assets/badges/scalajs-1.0.0-M5.svg)](https://www.scala-js.org) (& 0.6.25, react v16.4+)

A react library for scala written in the spirit of ReasonReact, a react library
for an OCaml dialect known as reason (sponsored by facebook). ReasonReact
documentation is located [here](https://reasonml.github.io/reason-react)
and provides a good description of how this library works since the API is very
similar. While more advanced scala functionality could have been used, the
scala.js implemetation was kept intentionally similar to ReasonReact so that the
documentation would apply.

This facade library is small, does not use advanced scala features and provides
a different component API based on reason-react vs reactjs. It explicitly
supports integrating into an existing javascript UI project or an entirely new
one.

You can always code a scala.js based react interface without using any facade
library since scala.js supports non-native JS class definitions. scala.js's JS
class support is extensive and well thought out. One of the reasons I liked the
ReasonReact model is that it rethought the abstractions and API. Instead of
trying to copy the reactjs API, it provides something smaller and emphasizes
changes in a few dimensions of the reactive UI API problem--almost the very
definition of "disruptive."

scalajs-react emphasizes integration into an existing project by making it easy
to import/export components and fit into existing application including those
using global state-managed solutions such as redux. At the same time, it allows
you to build your entire interface in scalajs-react. As long as your front-end
solution can manage the model of scala.js's output (one large module for all
scala.js code, not file-by-file/module-by-module), you should consider
scalajs-react for your solution.

* [Demo](http://aappddeevv.github.io/scalajs-react/static/index.html).
* [Live Coding](https://www.youtube.com/watch?v=7on-oT2Naco).

scalajs-react supports fragments and the new context provider in react v16.3. A
future version will support react-native. The scalaj-react API roughly mimics
ReasonReact 4.1 and the intent is to track that API and stick closely to
it. Read those docs to understand this API.

A g8 template is available. Use `sbt new aappddeevv/scalajs-react-app.g8` to create a
new project.

## Usage
Include the library in your build:
```scala
resolvers += Resolver.bintrayRepo("aappddeevv", "maven")
val scalaJsReactVersion = "latest.version"
//  or
//val scalaJsReactVersion = "0.1.0-M7"

// grab the the latest version or use a specific version
libraryDependencies ++= Seq(
    "ttg" %%% "scalajs-react-core" % scalaJsReactVersion,
    "ttg" %%% "scalajs-react-vdom" % scalaJsReactVersion,

    // optionals
    // if you need react-dom
    "ttg" %%% "scalajs-react-react-dom" % scalaJsReactVersion

    // Microsoft fabric UI components, "MS office", css-in-"scala"
    // css-in-scala can be used independently of fabric
    "ttg" %%% scalajs-react-fabric" % scalaJsReactVersion,
    
    // if you integrate with redux
    "ttg" %%% "scalajs-react-redux" % scalaJsReactVersion
    
    // if you need prop-types
    "ttg" %%% "scalajs-react-prop-types" % scalaJsReactVersion)
```

Do not forget to include the react libraries in your execution environment. For
react 16+, the libraries have been split out into multiple libraries. For
the reactjs based modules, the javascript dependencies are:

* core: react, create-react-class
* react-dom: react-dom

```sh
npm i --save react create-react-class
npm i --save react-dom
```

The latest react is recommended, v16.4. Note that using 16.4 will result on console messages for deprecated API that is still used by ReasonReact, and hence, scalajs-react.

### Create a Component
You can quickly create a component and render it:
```scala
object HelloWorld {
  val c = statelessComponent("HelloWorld")
  import c.ops._
  // could also use "aplly" so that you do HelloWorld() instead of HelloWorld.make(), but make is ReasonReact friendly.
  def make(name: Option[String]) =
    render{ self =>
        div("hello world" + name.map(" and welcome " + _).getOrElse(""))
      }
}

object MyWebApp {
   def app() = {
     ReactDom.renderToElementId(
      HelloWorld.make(Some("John")),
      "container"
      )
   }
}
```

You can add a className to the div using: `div(new DivProps { className =
"divClassName" })(...children...)`. `DivProps` is a non-native JS trait that
ensure you only enter valid div props.

The "render" method copies the internal proxy component and adds a callback from
reactjs to scala. If you need to use other methods, use the full syntax as the
render syntax is just a shortcut to:

```scala
    def make(name: Option[String]) =
      c.copy(new methods  {
        val render = self => { ... }
        didMount = js.defined{ oldNewSelf => 
            ... 
        } 
      })

```

The `val` is required on required methods, such as render, you must define it
otherwise you will get a syntax error. Optional methods are defined as shown
immediately above.

Reason-react uses `make` for its function name. However, you could use `apply`
which is more idiomatic scala.

### Exporting a Component to Javascript

If you want to use it in javascript or have a different "make" interface, define
the export and map the properties:

```scala
// optionally define a trait to make it easier to typecheck component parameters
trait HelloWorldProps extends js.Object {
  var name: js.UndefOr[String] = js.undefined
}

object HelloWorld {
...

  // Exported to javascript world: <HelloWorld name={"John"} />
  @JSExportTopLevel("HelloWorld")
  private val jsComponent =
    HelloWorld.wrapScalaForJs((jsProps: HelloWorldProps) => make(jsProps.name.toOption))
```

You can define your own "make" API to be whatever suits you:

```scala
  // Alternative scala `make` definition, render must convert to scala objects if needed
  // and internal scala code would need to create a HelloWorldProps object.
  def make2(props: HelloWorldProps = noProps()) =
    c.render{ self =>
        div(
          "hello world" + props.name.toOption.map(" and welcome " + _).getOrElse("")
        )
      }
```

Attributes are type checked properly and restricted to what you
specified. Here's how you would use it:

```scala
  HelloWorld.make2(new HelloWorldProps { name = "John" })
```

Most times the javascript interop has to deal with optional parameters and the conversion
from jsProps to `make` parameters is much more messy. If you do not need to use
the class in javascript, you can use standard scala parameters conventions as in `make`.

### Importing a Component from Javascript

If you need to import a javascript class, you can use the standard scala.js
machinery to import it:

```scala
@js.native
@JSImport("some-package", JSImport.Namespace)
object SomePackageNS extends js.Object {
  val label: ReactJsComponent = js.native
}
object SomePackage {
  import ttg.react.elements._
  def Label(props: Attr*)(children: ReactNode*) = wrapJsForScala(SomePackageNS.label, new Attrs(props).toJs, children:_*)
}
```

As with any scala side component, you can adapt the scala side API to be pretty
much anything:

```scala
object SomePackage {
  // Use a non-native JS trait
  def Label(props: js.UndefOr[LabelProps])(children: ReactNode*) = 
     wrapJsForScala(SomePackageNS.Label, props.getOrElse(noProps), children:_*)

  trait LabelProps extends HTMLAttrbutes[dom.html.Label] { // optionally extend HTMLAttributes[]
   // ...
  }
  
  // or
  def Label(someProp: String)(children: ReactNode*) = 
     // createProps a function you write to convert the single scala someProp parameter to a js.Object.
     wrapJsForScala(SomePackageNS, createProps(someProp), children:_*)
    
```

For importing, you need to call `wrapJsForScala` at some point. You are free to
use the model above for attributes and children or you can use a dedicated
non-native JS trait or use whatever suits your application for scala-side
API. You could also define the component to take `Attr*` props, a non-native JS
trait or even `js.Dynamic`.

Since functions are used, there are many advanced scala and scala.js patterns
you can use to alter the way that components are created and the API to create
elements.For example, you could also use type classes to change the attributes
to a js.Object since all you need is some type of "writer."

There is no JSX support.

### Styling

Styling can be performed different ways. For a quick summary of different
aspects of styling designs see this
[blog](http://appddeevvmeanderings.blogspot.com/2017/08/web-app-styling-interlude-how-to.html).

fabric's `merge-styles` is provided in the fabric package and you can use
css-in-scala concepts to create your styles easily. See the ToDo example source
code for a detailed example. It works nicely and is independent of fabric. Tree
shaking should remove any fabric UI code if you only want to use `merge-styles`
but not the UI part, and sill want a lean bundle. `merge-styles` creates
stylesheets from scala code. Here's an example showing the use of CSS
variables. `merge-styles` is a little like glamor (which we recommend as well)
but is also a bit different in that it is explicit about "selectors".

```scala
  // Each key indicates an area of our component.
  @js.native
  trait ToDoClassNames extends js.Object {
    var root: String      = js.native
    var todo: String      = js.native
    var title: String     = js.native
    var dataEntry: String = js.native
  }

  def getClassNames(width: Int = 400) =
    FabricStyling.mergeStyleSets[ToDoClassNames](
      styleset(
        "root" -> new IRawStyle {
          selectors = selectorset(
            ":global(:root)" -> lit(
              "--label-width" -> s"${width}px",
            ))
         },
         "todo" -> new IRawStyle {
          displayName = "machina"
          display = "flex"
          marginBottom = "10px"
          selectors = selectorset("& $title" -> new IRawStyle {})
        },
        "title" -> new IRawStyle {
          width = "var(--label-width)"
          marginRight = "10px"
        },
        "dataEntry" -> new IRawStyle {
          display = "flex"
          selectors = selectorset("& .ms-Textfield" -> new IRawStyle {
            width = "var(--label-width)"
            marginRight = "10px"
          })
        }
      ))
}

// now use it

val cn = getClassNames()
val props = new DivProps {
  className = cn.root
}
```

If you have dynamically calculated styles, its best to memoize the result so you
do not incur the processing overhead on each component creation. The above was
static so we just create it once. To memoize, use yours or fabric's
memoize. Fabric's `memoize` needs a js function, so:

```scala
// notice the fromFunction1 conversion from scala to js.Function, the "N" is for arity
val memoizedClassNames = fabric.UtilitiesNS.memoizeFunction(js.Any.fromFunction1(getClassName))
val cn = memoized(500)
```

If you keep your CSS external and process it at build time, you can just import
your style sheets and have them picked up by your bundler such as webpack:

```scala
@js.native
@JSImport("Examples/todo/todo.css", JSImport.Namespace)
object componentStyles extends js.Object

object styles {
  // or cast to js.Dictionary or anything else you want to use
  val component = componentStyles.asInstanceOf[js.Dynamic]
}

// use: styles.component.root
```

In the example above, you would need to ensure webpack has an alias to the
Examples directory and that you have setup your CSS processors
correctly. post-css is quite popular when using webpack. Since a Dynamics was
used, this is not type safe.

A styling trait is provided to help you ensure you only provide style attributes
for inline styling:

```scala
val hstyle = new StyleAttr { display = "flex" }

val props = new DivProps {
    style = hstyle
}
```

Styling can also be performed using scala specific libraries such as 
[ScalaCSS](https://github.com/japgolly/scalacss). 

### Redux
Redux integration requires you to export your component then import it with
redux enhanced props. Please see the extended documentation for details.

### Routing
A simple 100-line router is included based on the reason-react router
implementation. See the example app for an example of how to use it.

# Documentation

Client:
* [user](http://aappddeevv.github.io/scalajs-react)

Integrated API documentation:
* [all modules](https://aappddeevv.github.io/scalajs-react/api/ttg/react)

# Demo

You can start the demo locally if you run `sbt npmRunDemo`. A browser page
should open automatically. If not, point your browser to
[https://localhost:8080](https://localhost:8080). You can also just load the
demo files (after its built) into your browser but note that the icons will not
render correctly since they require a fetch.

# Motivation

I was looking for a react facade that rethought reactive interactions. Many of
the existing facades are straight adaptions of the standard react library and
make it quite easy to program react in a scala environment.

The ReasonReact facade, using a modified OCaml syntax, keeps it quite
simple. This library is actually quite small and simple and relies on
functions. Scala.js easily supports creating classes that map into javascript
classes and hence into react component classes. However, with parallel fiber
coming to react and other enhancements that are planned, a more functional
library should withstand the change in the core react infrastructure to
something that is more sustainable in the long run. The current react approach
makes it easy to introduce errors both at compile time and runtime. Even with
the use of typescript, it's still quite a clunky API. Of course it is clunky for
alot of "reasons" which is why Reason and ReasonReact was born. Facebook took an
innovative approach to structuring Reason. Instead of trying to make a js
superscript, which is what typescript does well, it is trying to go orthogonal
like many other javascript transpiled languages have done e.g. elm, dart and of
course scala.js.

## Suitability
Scala.js requires a bundling model that does not allow it to be broken apart and
deployed on a pure "file/module" basis. Because of this, scalajs-react is best
suited for UIs where it is not required to code split the scala portion of the
library unless some type of fancy splitter can split apart optimized scala.js
emitted code. Scala.js makes use of optimizations, such as inlining, and
integrated type infrastructure that makes it difficult to split.

The core scala.js infrastructure costs you about 2.5k and increases as you use
features such as immutability, the collections library or, of course, add data
structures to your code.

## Related
There are a few [scala.js](https://www.scala-js.org/) react facades/implementations available:
* https://github.com/eldis/scalajs-react: Very clean class oriented react implementation. Class oriented but has a builder as well and includes purely functional (stateless) component support as well. No macros. Allows ES class-like syntax. Has wrappers for japgolly. This lib was created to get around "wrapping" and other artifacts that the author was not fond of in japgolly.
* https://github.com/eldis/scalajs-redux: Redux facade by the same as above.
* https://github.com/shogowada/scalajs-reactjs: More functionally oriented facade. Contains redux facades and more.
* https://github.com/japgolly/scalajs-react: The can't shoot yourself in the foot implementation. well supported and thought out but a bit more complex API wise.
* https://slinky.shadaj.me: Newcomer. Uses macros smartly. Allows you to use scala.js components in js as well. Uses some macros to help with javascript interop.
* https://github.com/xored/scala-js-react: Class oriented facade. Uses macros to transform xml literals so you can write xml in your code like jsx.
* https://github.com/scalajs-react-interface/sri#sri: React-native and web. New maintainers.
* https://github.com/Ahnfelt/react4s: React wrapper. This does not just mimic the standard react interface. Includes an interesting CSS builder, css-in-scala.

The facades differ in their facade approach but many recreate all of the API
parts found in reactjs. japgolly works hard to put a more safe, functional API
in place from the outset. Some of these facades have alot of complex facade
components to help deal with the type and side-effects flexibility found in
react.

In theory, all of these can "export" a scala.js component to be used in
javascript environments. However, various libraries that go deep into scala-land
means that they are harder to use from javascript land often because they wrap
the javascript props and state objects in a way that makes them less usable from
the javascript world. Most, if not all, libraries allow you to use javascript
defined components fairly directly via some type of adoption or import process.

Libraries you may be interested in:

* [binding.scala](https://github.com/ThoughtWorksInc/Binding.scala): reactive UI framework. It uses a new binding framework for scala. Works on jvm and
js. The binding framework is at a individual element level like mobx. You can
use xml syntax as well. The binding approach is called "precise"
binding.
* [diode](https://github.com/suzaku-io/diode): redux replacement.
* [laminar](https://github.com/raquo/laminar): a reactive web framework.
* [levsha](https://github.com/fomkin/levsha): Fast pure scala.js virtual dom.
* [pine](https://github.com/sparsetech/pine): XML/HTML builder.
* [scalacss](https://github.com/japgolly/scalacss): A solid css-in-scala solution.
* [scala-dom-types](https://github.com/raquo/scala-dom-types): for dom attributes.
* [scala-tags](https://github.com/lihaoyi/scalatags): XML/HTML builder.
* [outwatch](https://github.com/OutWatch/outwatch/): reactive UI framework.
* [udash](https://udash.io/) is another reactive framework that is not
react based, but reactive.
* [udash-css](https://udash.io): A css-in-scala framework.

## License

MIT license. See the LICENSE file.

Copyright 2018 The Trapelo Group LLC.
