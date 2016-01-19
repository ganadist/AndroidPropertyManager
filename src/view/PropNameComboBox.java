package view;

import lib.Java2sAutoComboBox;

import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.List;

/**
 * Created by dbgsprw on 16. 1. 11.
 */
public class PropNameComboBox extends Java2sAutoComboBox {


    public PropNameComboBox(List list) {
        super(list);
        setStrict(false);
        addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent itemEvent) {
                if (itemEvent.getStateChange() == ItemEvent.SELECTED) {
                    PropNameComboBox propNameComboBox = (PropNameComboBox) itemEvent.getSource();
                }
            }
        });

    }

    @Override
    public void addItem(Object item) {
        super.addItem(item);
        getDataList().add(item.toString());
    }

}