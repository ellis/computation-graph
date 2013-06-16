package examples


import scalafx.Includes._
import scalafx.application.JFXApp
import scalafx.geometry.Pos
import scalafx.scene.Scene
import scalafx.scene.control.CheckBox
import scalafx.scene.control.Label
import scalafx.scene.control.TextField
import scalafx.scene.layout.VBox
import scalafx.scene.shape.Rectangle
import scalafx.stage.Stage
import scalafx.scene.input.KeyEvent

import javafx.scene.paint.Color

object World extends JFXApp {
  val name = new TextField
  val yell = new CheckBox("Yell?")
  val greeting = new Label {
  	text <== name.text
  }
  name.onKeyPressed = (e: KeyEvent) => {
  	println("event")
  }
  stage = new JFXApp.PrimaryStage {
    title = "Hello World"
    width = 600
    height = 450
    scene = new Scene {
      //fill = Color.LIGHTGREEN
      /*content = new Rectangle {
        x = 25
        y = 40
        width = 100
        height = 100
        fill <== when(hover) then Color.GREEN otherwise Color.RED
      }*/
      content = new VBox {
      	alignment = Pos.CENTER
      	content = Seq(
      		new Label("Name:"),
      		name,
      		yell,
      		greeting
      	)
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