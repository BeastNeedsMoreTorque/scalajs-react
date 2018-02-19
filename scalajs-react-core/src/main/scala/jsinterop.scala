// Copyright (c) 2018 The Trapelo Group LLC
// This software is licensed under the MIT License (MIT).
// For more information see LICENSE or https://opensource.org/licenses/MIT

package ttg
package react

import scala.scalajs.js
import js.annotation._
import js.|
import js._
import js.Dynamic.{literal => lit}

import org.scalajs.dom

// https://github.com/chandu0101/sri/blob/a5fb8db2cc666299ecc863d6421994cce5d304e6/core/src/main/scala/sri/core/React.scala
@js.native
private[react] trait JSReact extends js.Object {

  /** Can take a wide variety of types for tpe: string | sfc | class (extending React.Component) */
  def createElement[P](el: js.Any | String, props: UndefOr[P], children: ReactNode*): ReactDOMElement = js.native
  def cloneElement(el: ReactElement, props: js.Dynamic): ReactDOMElement = js.native
  // symbol or number depending on browser/environment support for symbols
  val Fragment: js.Any = js.native
  /** v16.3 API. */
  def createContext[T](defaultValue: T, calculateChangedBits: js.UndefOr[js.Function2[T, T, Int]]): ReactContext[T] = js.native
}

@js.native
@JSImport("react", JSImport.Namespace)
private[react] object JSReact extends JSReact

object React {
  def createElement[P <: js.Object](tag: String, props: P)(children: ReactNode*): ReactDOMElement = JSReact.createElement(tag, props, children: _*)

  def createElement(tag: ReactClass, props: js.Object)(children: ReactNode*): ReactDOMElement = JSReact.createElement(tag, props, children: _*)

  def createElement(tag: String)(children: ReactNode*): ReactDOMElement =
    JSReact.createElement(tag, js.undefined, children: _*)

  // def createElement(
  //   tag: ReactClass,
  //   props: js.Object,
  //   children: Seq[ReactNode]
  // ): ReactDOMElement = JSReact.createElement(tag, props, children:_*)

  // def createElement[P <: js.Object](
  //     c: ReactClass,
  //     props: P,
  //     children: Seq[ReactNode]
  // ): ReactDOMElement = JSReact.createElement(c, props, children: _*)

  // def createElement[P <: js.Object](
  //     c: ReactClass,
  //     props: js.UndefOr[P],
  //     children: Seq[ReactNode]
  // ): ReactDOMElement = JSReact.createElement(c, props, children: _*)

  def createElement(
      c: ReactClass
  ): ReactDOMElement = JSReact.createElement(c, js.undefined)

  /**
    * Create a react fragment. Fragments are created as an "element" with a specific
    * tag (symbol or number if target does not support symbol) vs say, the string "div".
    */
  def createFragment(key: Option[String], children: ReactNode*): ReactDOMElement = {
    val props = js.Dictionary.empty[js.Any]
    key.foreach(props("key") = _)
    JSReact.createElement(JSReact.Fragment, props, children: _*)
  }
}

/**
  * We use create-react-class under the hood to create all classes
  */
@js.native
@JSImport("create-react-class", JSImport.Default)
object reactCreateClass extends js.Object {
  def apply(props: js.Object): ReactClass = js.native
}

@js.native
trait JSReactDOM extends js.Object {
  def render(node: ReactNode, target: dom.Element): Unit = js.native
  def createPortal(node: ReactNode, target: dom.Element): ReactElement = js.native
  def unmountComponentAtNode(el: dom.Element): Unit = js.native
  //def findDOMNode(??? .reactRef): dom.element = js.native
}

@js.native
@JSImport("react-dom", JSImport.Namespace)
object JSReactDOM extends JSReactDOM

object reactdom {

  /** Render into the DOM given an element id. */
  def renderToElementWithId(el: ReactNode, id: String) = {
    val target = Option(dom.document.getElementById(id))
    target.fold(throw new Exception(s"renderToElementWithId: No element with id $id found in the HTML."))(htmlel => JSReactDOM.render(el, htmlel))
  }

  /** Render the DOM given an element id using react's portal. */
  def createPortalInElementWithId(node: ReactNode, id: String) = {
    val target = Option(dom.document.getElementById(id))
    target.fold(throw new Exception(s"createPortalInElemeentWithId: No element with id $id founud in the HTML."))(htmlel =>
      JSReactDOM.createPortal(node, htmlel))
  }
}
@js.native
@JSImport("prop-types", JSImport.Namespace)
object PropTypes extends ReactPropTypes {}

@js.native
trait Requireable[T <: js.Any] extends js.Object {
  def isRequired(obj: T, key: String, componentName: String, rest: js.Any*): js.Any = js.native
}

@js.native
trait ReactPropTypes extends js.Object {
  val `any`: Requireable[js.Any] = js.native
  val array: Requireable[js.Any] = js.native
  val bool: Requireable[js.Any] = js.native
  val func: Requireable[js.Any] = js.native
  val number: Requireable[js.Any] = js.native
  val `object`: Requireable[js.Any] = js.native
  val string: Requireable[js.Any] = js.native
  val node: Requireable[js.Any] = js.native
  val element: Requireable[js.Any] = js.native
  def instanceOf(expectedClass: js.Object): Requireable[js.Any] = js.native
  def oneOf(types: js.Array[js.Any]): Requireable[js.Any] = js.native
  def oneOfType(types: js.Array[Requireable[js.Any]]): Requireable[js.Any] = js.native
  def arrayOf(`type`: Requireable[js.Any]): Requireable[js.Any] = js.native
  def objectOf(`type`: Requireable[js.Any]): Requireable[js.Any] = js.native
  def shape(`type`: Requireable[js.Any]): Requireable[js.Any] = js.native
}

@js.native
trait ReactContext[T] extends js.Object {
  type ValueType = T
  /** Only takes a single attribute value, "value" with the context . */
  val Provider: js.Any = js.native
  val Consumer: js.Any = js.native
  /** Not public API. */
  var currentValue: T = js.native
  /** Not public API. */
  val defaultValue: T = js.native  
}

// @todo Allow key on consumer...
object context {

  /** v16.3 API. */
  def make[T](defaultValue: T): ReactContext[T] = JSReact.createContext[T](defaultValue, js.undefined)

  def makeProvider[T](ctx: ReactContext[T])(value: Option[T])(children: ReactNode*): ReactNode = {
    val v = lit("value" -> value.getOrElse(ctx.currentValue).asInstanceOf[js.Any])
    JSReact.createElement(ctx.Provider, v, children:_*)
  }

  def makeConsumer[T](ctx: ReactContext[T])(f: js.Function1[T, ReactNode]): ReactNode =
    JSReact.createElement(ctx.Consumer, js.undefined, f.asInstanceOf[ReactNode])

  /** `import context._` brings the syntax into scope. */
  implicit class ReactContextOps[T](ctx: ReactContext[T]) {
    def makeProvider(value: T)(children: ReactNode*) = context.makeProvider[T](ctx)(Some(value))(children:_*)
    def makeProvider(children: ReactNode*) = context.makeProvider[T](ctx)(None)(children:_*)
    def makeConsumer(f: js.Function1[T, ReactNode]) = context.makeConsumer[T](ctx)(f)
  }
}
