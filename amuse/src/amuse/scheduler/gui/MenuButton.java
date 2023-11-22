package amuse.scheduler.gui;

import javax.swing.JPopupMenu;
import javax.swing.JToggleButton;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import java.awt.event.ActionListener;
import java.awt.Dimension;
import java.awt.event.ActionEvent;

public class MenuButton extends JToggleButton {

    JPopupMenu popup;

    public MenuButton(String name, JPopupMenu menu) {
        super(name);
        this.popup = menu;
        addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ev) {
                JToggleButton button = MenuButton.this;
                popup.setPreferredSize(new Dimension(button.getBounds().width, button.getBounds().height*menu.getComponentCount()));
                if (button.isSelected()) {
                    popup.show(button, 0, button.getBounds().height);
                } else {
                    popup.setVisible(false);
                }
            }
        });
        popup.addPopupMenuListener(new PopupMenuListener() {
            @Override
            public void popupMenuWillBecomeVisible(PopupMenuEvent e) {}
            @Override
            public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
                MenuButton.this.setSelected(false);
            }
            @Override
            public void popupMenuCanceled(PopupMenuEvent e) {}
        });
    }
}
