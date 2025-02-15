package org.hzt.component;

import org.hzt.Resources;
import org.hzt.event.EventMenuSelected;
import org.hzt.model.ModelMenu;
import org.hzt.swing.ListMenu;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

import static javax.swing.GroupLayout.*;

public final class Menu extends JPanel {

    private final ListMenu listMenu1 = new ListMenu();
    private final JPanel panelMoving = new JPanel();

    public void addEventMenuSelected(EventMenuSelected event) {
        listMenu1.addEventMenuSelected(event);
    }

    public Menu() {
        initComponents();
        setOpaque(false);
        listMenu1.setOpaque(false);
        init();
    }

    private void init() {
        listMenu1.addItem(new ModelMenu("1", "Dashboard", ModelMenu.MenuType.MENU));
        listMenu1.addItem(new ModelMenu("2", "UI Elements", ModelMenu.MenuType.MENU));
        listMenu1.addItem(new ModelMenu("3", "Comonents", ModelMenu.MenuType.MENU));
        listMenu1.addItem(new ModelMenu("4", "Forms Stuff", ModelMenu.MenuType.MENU));
        listMenu1.addItem(new ModelMenu("5", "Date Table", ModelMenu.MenuType.MENU));
        listMenu1.addItem(new ModelMenu("", " ", ModelMenu.MenuType.EMPTY));

        listMenu1.addItem(new ModelMenu("", "My Data", ModelMenu.MenuType.TITLE));
        listMenu1.addItem(new ModelMenu("", " ", ModelMenu.MenuType.EMPTY));
        listMenu1.addItem(new ModelMenu("6", "Icons", ModelMenu.MenuType.MENU));
        listMenu1.addItem(new ModelMenu("7", "Sample Page", ModelMenu.MenuType.MENU));
        listMenu1.addItem(new ModelMenu("8", "Extra", ModelMenu.MenuType.MENU));
        listMenu1.addItem(new ModelMenu("9", "More", ModelMenu.MenuType.MENU));
        listMenu1.addItem(new ModelMenu("10", "Logout", ModelMenu.MenuType.MENU));
        listMenu1.addItem(new ModelMenu("", "", ModelMenu.MenuType.EMPTY));
    }

    private void initComponents() {
        var jLabel1 = new JLabel();

        panelMoving.setOpaque(false);

        jLabel1.setFont(new Font("sansserif", Font.BOLD, 18));
        jLabel1.setForeground(new Color(255, 255, 255));
        jLabel1.setIcon(new ImageIcon(Resources.urlOrThrow("/org/hzt/icon/logo.png")));
        jLabel1.setText("Application");

        javax.swing.GroupLayout panelMovingLayout = new javax.swing.GroupLayout(panelMoving);
        panelMoving.setLayout(panelMovingLayout);
        panelMovingLayout.setHorizontalGroup(
            panelMovingLayout.createParallelGroup(Alignment.LEADING)
            .addGroup(panelMovingLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel1, DEFAULT_SIZE, 203, Short.MAX_VALUE)
                .addContainerGap())
        );
        panelMovingLayout.setVerticalGroup(
            panelMovingLayout.createParallelGroup(Alignment.LEADING)
            .addGroup(Alignment.TRAILING, panelMovingLayout.createSequentialGroup()
                .addGap(15, 15, 15)
                .addComponent(jLabel1)
                .addContainerGap())
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(Alignment.LEADING)
            .addComponent(panelMoving, DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(listMenu1, DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(panelMoving, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
                .addGap(15, 15, 15)
                .addComponent(listMenu1, DEFAULT_SIZE, 414, Short.MAX_VALUE))
        );
    }

    @Override
    protected void paintChildren(Graphics grphcs) {
        Graphics2D g2 = (Graphics2D) grphcs;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        GradientPaint g = new GradientPaint(0, 0, Color.decode("#1CB5E0"), 0, getHeight(), Color.decode("#000046"));
        g2.setPaint(g);
        g2.fillRoundRect(0, 0, getWidth(), getHeight(), 15, 15);
        g2.fillRect(getWidth() - 20, 0, getWidth(), getHeight());
        super.paintChildren(grphcs);
    }

    private int x;
    private int y;

    public void initMoving(JFrame frame) {
        panelMoving.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent me) {
                x = me.getX();
                y = me.getY();
            }

        });
        panelMoving.addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseDragged(MouseEvent me) {
                frame.setLocation(me.getXOnScreen() - x, me.getYOnScreen() - y);
            }
        });
    }
}
