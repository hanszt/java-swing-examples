package hzt;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import java.awt.*;

/// [adding-jbutton-to-jtable](https://stackoverflow.com/questions/13833688/adding-jbutton-to-jtable)
final class JButtonTableSample {

    JButtonTableSample() {
        final var dm = new DefaultTableModel();
        var columnIdentifiers = new String[]{"Button", "String"};
        var dataVector = new Object[][]{
                {"button 1", "foo"},
                {"button 2", "bar"},
                {"button 3", "boo"}
        };
        dm.setDataVector(dataVector, columnIdentifiers);

        final var table = new JTable(dm);
        var buttonColumn = table.getColumn("Button");
        buttonColumn.setCellRenderer(new ButtonRenderer());
        buttonColumn.setCellEditor(new ButtonEditor());


        table.setPreferredScrollableViewportSize(table.getPreferredSize());//thanks mKorbel +1 http://stackoverflow.com/questions/10551995/how-to-set-jscrollpane-layout-to-be-the-same-as-jtable

        table.getColumnModel().getColumn(0).setPreferredWidth(100);//so buttons will fit and not be shown butto..

        final var frame = new JFrame("JButtonTable Example");
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.add(new JScrollPane(table));
        frame.pack();
        frame.setVisible(true);
    }

    static void main() {
        SwingUtilities.invokeLater(JButtonTableSample::new);
    }
}

final class ButtonRenderer implements TableCellRenderer {

    private final JButton button = new JButton(); // Used as a template for the button in the rendering phase

    public ButtonRenderer() {
        button.setOpaque(true);
    }

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value,
                                                   boolean isSelected, boolean hasFocus, int row, int column) {
        if (isSelected) {
            button.setForeground(table.getSelectionForeground());
            button.setBackground(table.getSelectionBackground());
        } else {
            button.setForeground(table.getForeground());
            button.setBackground(UIManager.getColor("Button.background"));
        }
        button.setText((value == null) ? "" : value.toString());
        return button;
    }
}

final class ButtonEditor extends DefaultCellEditor {

    private final JButton button = new JButton(); // Used as a template for the button in the editor phase
    private String label;
    private boolean isPushed;

    public ButtonEditor() {
        super(new JCheckBox());
        button.setOpaque(true);
        button.addActionListener(_ -> fireEditingStopped());
    }

    @Override
    public Component getTableCellEditorComponent(final JTable table, final Object value,
                                                 final boolean isSelected, final int row, final int column) {
        if (isSelected) {
            button.setForeground(table.getSelectionForeground());
            button.setBackground(table.getSelectionBackground());
        } else {
            button.setForeground(table.getForeground());
            button.setBackground(table.getBackground());
        }
        label = (value == null) ? "" : value.toString();
        button.setText(label);
        isPushed = true;
        return button;
    }

    @Override
    public Object getCellEditorValue() {
        if (isPushed) {
            JOptionPane.showMessageDialog(button, label + ": Ouch!");
        }
        isPushed = false;
        return label;
    }

    @Override
    public boolean stopCellEditing() {
        isPushed = false;
        return super.stopCellEditing();
    }
}
