package com.ethereal.ymd;

import com.ethereal.ymd.view.MainForm;
import javax.swing.*;

/**
 * @author Slava
 */
public class Main {


    public static void main(String[] args) throws Exception{
        UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new MainForm();
            }
        });
    }

}
