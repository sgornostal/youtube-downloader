package com.ethereal.ymd.view;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.URL;
import java.util.List;
import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.*;

import com.ethereal.ymd.utils.RuntimeUtils;
import com.ethereal.ymd.youtube.Quality;
import com.ethereal.ymd.youtube.YoutubeResult;
import com.intellij.uiDesigner.core.*;
import org.apache.log4j.Logger;

/**
 * @author Slava
 */
public class YoutubeResultView extends JPanel {

    private static final Logger logger = Logger.getLogger(YoutubeResultView.class);
    private YoutubeResult selectedYoutubeResult;
    private Quality selectedQuality;

    public YoutubeResult getSelectedYoutubeResult() {
        return chbDownloadable.isSelected()?selectedYoutubeResult:null;
    }

    public void setDownloadable(boolean downloadable) {
        chbDownloadable.setSelected(downloadable);
    }

    public Quality getSelectedQuality() {
        return selectedQuality;
    }

    @SuppressWarnings("unchecked")
    public YoutubeResultView(final List<YoutubeResult> resultList) {
        initComponents();
        selectedYoutubeResult = resultList.get(0);
        for(YoutubeResult youtubeResult:resultList) {
            cbValue.addItem(youtubeResult);
        }
        cbValue.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                selectedYoutubeResult = (YoutubeResult) cbValue.getSelectedItem();
                cbQuality.removeAllItems();
                try {
                    final Image image = ImageIO.read(new URL(selectedYoutubeResult.getThumbnail()));
                    lThumbnail.setIcon(new ImageIcon(image));
                } catch (IOException exIm) {
                    lThumbnail.setText(selectedYoutubeResult.getThumbnail());
                    logger.error(String.format("Id = [%s] Error while loading thumbnail.",
                            selectedYoutubeResult.getVideoId()), exIm);
                }
                try {
                    final List<Quality> qualityList = RuntimeUtils.getVideoQualities(selectedYoutubeResult.getVideoId());
                    for(Quality quality:qualityList) {
                        cbQuality.addItem(quality);
                    }
                } catch (Exception ex) {
                    cbQuality.addItem(new Quality(-1, "Invalid", "Invalid"));
                    logger.error(String.format("Id = [%s] Error while loading qualities.",
                            selectedYoutubeResult.getVideoId()), ex);
                }
                cbQuality.setSelectedIndex(cbQuality.getItemCount()-1);
                lThumbnail.revalidate();
            }
        });

        cbQuality.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                selectedQuality = (Quality) cbQuality.getSelectedItem();
            }
        });

        cbValue.setSelectedIndex(0);
        cbQuality.setSelectedIndex(cbQuality.getItemCount()-1);
        chbDownloadable.setSelected(true);
    }

    private void initComponents() {
        // JFormDesigner - Component initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents
        // Generated using JFormDesigner Evaluation license - Vyacheslav Gornostal
        lThumbnail = new JLabel();
        cbValue = new JComboBox();
        cbQuality = new JComboBox();
        chbDownloadable = new JCheckBox();

        //======== this ========
        setBorder(new EtchedBorder());


        setLayout(new GridLayoutManager(1, 4, new Insets(0, 0, 0, 0), -1, -1));
        add(lThumbnail, new GridConstraints(0, 0, 1, 1,
            GridConstraints.ANCHOR_WEST, GridConstraints.FILL_BOTH,
            GridConstraints.SIZEPOLICY_CAN_SHRINK,
            GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
            null, null, null));
        add(cbValue, new GridConstraints(0, 1, 1, 1,
            GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL,
            GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
            GridConstraints.SIZEPOLICY_FIXED,
            null, null, null));
        add(cbQuality, new GridConstraints(0, 2, 1, 1,
            GridConstraints.ANCHOR_EAST, GridConstraints.FILL_NONE,
            GridConstraints.SIZEPOLICY_FIXED,
            GridConstraints.SIZEPOLICY_FIXED,
            null, null, null));
        add(chbDownloadable, new GridConstraints(0, 3, 1, 1,
            GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_NONE,
            GridConstraints.SIZEPOLICY_FIXED,
            GridConstraints.SIZEPOLICY_FIXED,
            null, null, null));
        // JFormDesigner - End of component initialization  //GEN-END:initComponents
    }

    // JFormDesigner - Variables declaration - DO NOT MODIFY  //GEN-BEGIN:variables
    // Generated using JFormDesigner Evaluation license - Vyacheslav Gornostal
    private JLabel lThumbnail;
    private JComboBox cbValue;
    private JComboBox cbQuality;
    private JCheckBox chbDownloadable;
    // JFormDesigner - End of variables declaration  //GEN-END:variables
}
