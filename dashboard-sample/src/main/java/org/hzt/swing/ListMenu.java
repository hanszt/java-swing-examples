package org.hzt.swing;

import org.hzt.event.EventMenuSelected;
import org.hzt.model.ModelMenu;
import org.hzt.model.ModelMenu.MenuType;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;

public final class ListMenu extends JList<ModelMenu> {

    private final DefaultListModel<ModelMenu> model;
    private int selectedIndex = -1;
    private int overIndex = -1;
    private EventMenuSelected event;

    public void addEventMenuSelected(final EventMenuSelected event) {
        this.event = event;
    }

    public ListMenu() {
        model = new DefaultListModel<>();
        setModel(model);
        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(final MouseEvent me) {
                if (SwingUtilities.isLeftMouseButton(me)) {
                    final var index = locationToIndex(me.getPoint());
                    final Object o = model.getElementAt(index);
                    if (o instanceof final ModelMenu menu) {
                        if (menu.type() == MenuType.MENU) {
                            selectedIndex = index;
                            if (event != null) {
                                event.selected(index);
                            }
                        }
                    } else {
                        selectedIndex = index;
                    }
                    repaint();
                }
            }

            @Override
            public void mouseExited(final MouseEvent me) {
                overIndex = -1;
                repaint();
            }
        });
        addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseMoved(final MouseEvent me) {
                final var index = locationToIndex(me.getPoint());
                if (index != overIndex) {
                    final Object o = model.getElementAt(index);
                    if (o instanceof final ModelMenu menu) {
                        if (menu.type() == MenuType.MENU) {
                            overIndex = index;
                        } else {
                            overIndex = -1;
                        }
                        repaint();
                    }
                }
            }
        });
    }

    @Override
    public ListCellRenderer<? super ModelMenu> getCellRenderer() {
        return new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(final JList<?> jlist, final Object o, final int index, final boolean selected, final boolean focus) {
                final ModelMenu data;
                if (o instanceof ModelMenu) {
                    data = (ModelMenu) o;
                } else {
                    data = new ModelMenu("", o + "", MenuType.EMPTY);
                }
                final var item = new MenuItem(data);
                item.setSelected(selectedIndex == index);
                item.setOver(overIndex == index);
                return item;
            }

        };
    }

    public void addItem(final ModelMenu data) {
        model.addElement(data);
    }
}
