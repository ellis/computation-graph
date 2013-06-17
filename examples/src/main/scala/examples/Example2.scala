package examples


import scalafx.Includes._
import scalafx.application.JFXApp
import scalafx.geometry.Pos
import scalafx.scene.Scene
import scalafx.scene.control.Button
import scalafx.scene.control.CheckBox
import scalafx.scene.control.Label
import scalafx.scene.control.TextField
import scalafx.scene.layout.HBox
import scalafx.scene.layout.VBox
import scalafx.scene.shape.Rectangle
import scalafx.scene.shape.Circle
import scalafx.stage.Stage
import scalafx.scene.input.KeyEvent
import scalafx.beans.value.ObservableValue
import javafx.scene.paint.Color
import scalafx.event.ActionEvent

object Example2 extends JFXApp {
	val add = new Button("Add Random Shape")
	val undo = new Button("Undo")
	val redo = new Button("Redo")
	val buttons = new HBox {
		content = Seq(add, undo, redo)
	}
	val shapes = new HBox
	stage = new JFXApp.PrimaryStage {
		title = "Undo/Redo Example"
		width = 600
		height = 450
		scene = new Scene {
			content = new VBox {
				alignment = Pos.CENTER
				content = Seq(
					buttons,
					shapes
				)
			}
		}
	}
	add.onAction = (e: ActionEvent) => {
		val shape = new Circle { radius = 40 }
		shapes.content.add(shape)
	}
}
