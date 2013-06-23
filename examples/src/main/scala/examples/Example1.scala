package examples


import scalafx.Includes._
import scalafx.application.JFXApp
import scalafx.geometry.Insets
import scalafx.geometry.Pos
import scalafx.scene.Scene
import scalafx.scene.control.CheckBox
import scalafx.scene.control.Label
import scalafx.scene.control.TextField
import scalafx.scene.layout.GridPane
import scalafx.scene.layout.VBox
import scalafx.scene.shape.Rectangle
import scalafx.scene.shape.Circle
import scalafx.stage.Stage
import scalafx.event.ActionEvent
import scalafx.scene.input.KeyEvent
import scalafx.beans.value.ObservableValue
import scalafx.scene.paint.Color

import ch.ethz.computationgraph._

object Example1 extends JFXApp {
	val call1 = Call(
		fn = (args: List[Object]) => {
			val s0 = if (args.head.toString.isEmpty) "Hello" else s"Hello, ${args.head}"
			val s1 = if (args(1).asInstanceOf[java.lang.Boolean]) s0.toUpperCase else s0
			List(
				CallResultItem_Entity("message", s1)
			)
		},
		args = Selector_Entity("name") :: Selector_Entity("yell") :: Nil
	)
	val call2 = Call(
		fn = (args: List[Object]) => {
			List(
				CallResultItem_Entity("color", (if (args.head.asInstanceOf[java.lang.Boolean]) Color.RED else Color.BLACK))
			)
		},
		args = Selector_Entity("yell") :: Nil
	)
	var cg = ComputationGraph()
	cg = cg.addCall(call1)
	cg = cg.addCall(call2)
	cg = cg.setImmutableEntity("name", "")
	cg = cg.setImmutableEntity("yell", false.asInstanceOf[java.lang.Boolean])
	cg = cg.step()

  val name = new TextField
  val yell = new CheckBox("")
  val message = new Label/* {
  	text <== when (yell.selected) then name.text + "!" otherwise name.text
  }*/
  name.onKeyReleased = (e: KeyEvent) => {
  	println("event")
    cg = cg.setImmutableEntity("name", name.getText())
    cg = cg.step()
    println(cg)
    val message_? = cg.db.getEntity(List(3), "message")
    message.setText(message_?.getOrElse("").toString)
  	/*greeting.text = {
	  	if (name.text.value.isEmpty)
	  		""
		else if (yell.selected.value)
			"HELLO, " + name.text.value.toUpperCase() + "!"
		else
			"Hello, " + name.text.value + "."
  	}*/
  }
  yell.onAction = (e: ActionEvent) => {
  	println("event")
  	val y = yell.selected.value
    cg = cg.setImmutableEntity("yell", y.asInstanceOf[java.lang.Boolean])
    cg = cg.step()
    println(cg)
    val message_? = cg.db.getEntity(List(3), "message").map(_.toString)
    message.setText(message_?.getOrElse(""))
    val color_? = cg.db.getEntity(List(3), "color").map(_.asInstanceOf[Color])
    message.textFill = color_?.getOrElse(Color.BLACK)
  }
  stage = new JFXApp.PrimaryStage {
    title = "Greetings"
    width = 600
    height = 450
    scene = new Scene {
      content = new GridPane {
      	alignment = Pos.CENTER
      	hgap = 10
      	vgap = 10
      	padding = Insets(25, 25, 25, 25)
      	add(new Label("Name:"), 0, 0)
      	add(name, 1, 0)
      	add(new Label("Yell:"), 0, 1)
      	add(yell, 1, 1)
      	add(new Label("Message:"), 0, 2)
      	add(message, 1, 2)
      }
    }
  }
}
/*
import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import ch.ethz.computationgraph._
//import scala.reflect.runtime.universe.typeOf

class HelloWorld extends Application {
	val call = Call(
		fn = (args: List[Object]) => {
			List(
				CallResultItem_Entity("message", s"Hello, ${args.head}!")
			)
		},
		args = Selector_Entity("name") :: Nil
	)
	var cg = ComputationGraph()
	cg = cg.addCall(call)
	cg = cg.setImmutableEntity("name", "")
	/*
	it("Call to `call0` should store output entity at next time step") {
		cg.processCall(call0, List(0))
		assert(cg.db.selectEntity(typeOf[String], "output0", List(0)) === None)
		assert(cg.db.selectEntity(typeOf[String], "output0", List(1)) === Some("Hello, World!"))
	}
	*/
	def start(primaryStage: Stage) {
		primaryStage.setTitle("Hello World!");
		val grid = new GridPane();
		grid.setAlignment(Pos.CENTER);
		grid.setHgap(10);
		grid.setVgap(10);
		grid.setPadding(new Insets(25, 25, 25, 25));

		val scenetitle = new Text("Welcome");
		scenetitle.setFont(Font.font("Tahoma", FontWeight.NORMAL, 20));
		grid.add(scenetitle, 0, 0, 2, 1);

		val userName = new Label("User Name:");
		grid.add(userName, 0, 1);

		val userTextField = new TextField();
		grid.add(userTextField, 1, 1);

		val pw = new Label("Password:");
		grid.add(pw, 0, 2);

		val pwBox = new PasswordField();
		grid.add(pwBox, 1, 2);
		
		val btn = new Button("Sign in");
		val hbBtn = new HBox(10);
		hbBtn.setAlignment(Pos.BOTTOM_RIGHT);
		hbBtn.getChildren().add(btn);
		grid.add(hbBtn, 1, 4);
		
		val actiontarget = new Text();
        grid.add(actiontarget, 1, 6);
        
        btn.setOnAction(new EventHandler[ActionEvent]() {
        	 
            def handle(e: ActionEvent) {
                actiontarget.setFill(Color.FIREBRICK);
                actiontarget.setText("Sign in button pressed");
                cg = cg.setImmutableEntity("name", userTextField.getText())
                cg = cg.step()
                println(cg)
                val message_? = cg.db.getEntity(List(2), "message")
                pw.setText(message_?.getOrElse("").toString)
            }
        });
		
		val scene = new Scene(grid, 300, 275);
		primaryStage.setScene(scene);
		primaryStage.show();
	}
}

object HelloWorld {
	def main(args: Array[String]) {
		Application.launch(classOf[HelloWorld], args : _*)
	}
}
*/