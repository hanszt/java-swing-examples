package hzt;

import hzt.colorpickersample.ColorPickerSample;
import hzt.comboboxsample.ComboBoxDemo;
import hzt.filechoosersample.FileChooserSample;
import hzt.physics_animation.DynamicsSimDemo;
import hzt.radiobuttonsample.RadioButtonDemo;
import hzt.search_algorithms.ASCIIMazeVisualizer;
import hzt.spinnersample.SpinnerDemo4;
import hzt.treedemoproject.TreeIconDemo;
import hzt.unitconvertersample.Converter;

public final class Main {

    private Main() {
    }

    static void main() {
        ColorPickerSample.main();
        ComboBoxDemo.main();
        FileChooserSample.main();
        SpinnerDemo4.main();
        Converter.main();
        TreeIconDemo.main();
        ASCIIMazeVisualizer.main();
        DynamicsSimDemo.main();
        RadioButtonDemo.main();
    }
}
