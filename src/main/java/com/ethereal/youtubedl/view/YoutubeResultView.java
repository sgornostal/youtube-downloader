package com.ethereal.youtubedl.view;

import com.ethereal.youtubedl.utils.RuntimeUtils;
import com.ethereal.youtubedl.youtube.Quality;
import com.ethereal.youtubedl.youtube.YoutubeResult;
import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import org.apache.log4j.Logger;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.EtchedBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * @author Slava
 */
public class YoutubeResultView extends JPanel {

    private static final Logger logger = Logger.getLogger(YoutubeResultView.class);

    private YoutubeResult selectedYoutubeResult;
    private Quality selectedQuality;

    public YoutubeResult getSelectedYoutubeResult() {
        return chbDownloadable.isSelected() ? selectedYoutubeResult : null;
    }

    public void setDownloadable(boolean downloadable) {
        chbDownloadable.setSelected(downloadable);
    }

    public Quality getSelectedQuality() {
        return selectedQuality;
    }

    public void changeDownloadProgress(int value) {
        pDownloadProgress.setValue(value);
    }

    @SuppressWarnings("unchecked")
    public YoutubeResultView(final List<YoutubeResult> resultList) {
        initComponents();
        selectedYoutubeResult = resultList.get(0);
        for (YoutubeResult youtubeResult : resultList) {
            cbValue.addItem(youtubeResult);
        }
        cbValue.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                selectedYoutubeResult = (YoutubeResult) cbValue.getSelectedItem();
                cbQuality.removeAllItems();

                new SwingWorker<Void, Void>() {
                    @Override
                    protected Void doInBackground() throws Exception {
                        cbValue.setEnabled(false);
                        cbQuality.setEnabled(false);
                        chbDownloadable.setEnabled(false);
                        final ExecutorService executorService = Executors.newFixedThreadPool(3);

                        executorService.submit(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    final Image image = ImageIO.read(new URL(selectedYoutubeResult.getThumbnail()));
                                    lThumbnail.setIcon(new ImageIcon(image));
                                } catch (IOException exIm) {
                                    lThumbnail.setText(selectedYoutubeResult.getThumbnail());
                                    logger.error(String.format("Id = [%s] Error while loading thumbnail.",
                                            selectedYoutubeResult.getVideoId()), exIm);
                                }
                            }
                        });
                        executorService.submit(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    final List<Quality> qualityList = RuntimeUtils.getVideoQualities(selectedYoutubeResult.getVideoId());
                                    for (Quality quality : qualityList) {
                                        cbQuality.addItem(quality);
                                    }
                                } catch (Exception ex) {
                                    cbQuality.addItem(new Quality(-1, "Invalid", "Invalid"));
                                    logger.error(String.format("Id = [%s] Error while loading qualities.",
                                            selectedYoutubeResult.getVideoId()), ex);
                                }
                            }
                        });
                        executorService.submit(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    String duration = RuntimeUtils.getVideoDuration(selectedYoutubeResult.getVideoId());
                                    lDuration.setText(duration);
                                } catch (Exception ex) {
                                    lDuration.setText("00:00");
                                    logger.error(String.format("Id = [%s] Error while loading duration.",
                                            selectedYoutubeResult.getVideoId()), ex);
                                }
                            }
                        });
                        executorService.shutdown();
                        try {
                            executorService.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
                        } catch (InterruptedException e) {
                            logger.error(e);
                        }
                        cbQuality.setSelectedIndex(cbQuality.getItemCount() - 1);
                        return null;
                    }

                    @Override
                    protected void done() {
                        cbValue.setEnabled(true);
                        cbQuality.setEnabled(true);
                        chbDownloadable.setEnabled(true);
                    }
                }.execute();
            }
        });

        cbQuality.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                selectedQuality = (Quality) cbQuality.getSelectedItem();
            }
        });

        cbValue.setSelectedIndex(0);
        cbQuality.setSelectedIndex(cbQuality.getItemCount() - 1);
        chbDownloadable.setSelected(true);
    }

    private void initComponents() {
        // JFormDesigner - Component initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents
        pInfo = new JPanel();
        lThumbnail = new JLabel();
        cbValue = new JComboBox();
        lDuration = new JLabel();
        cbQuality = new JComboBox();
        chbDownloadable = new JCheckBox();
        pDownloadProgress = new JProgressBar();

        //======== this ========
        setBorder(new EtchedBorder());
        setLayout(new GridLayoutManager(2, 1, new Insets(0, 0, 0, 0), -1, -1));

        //======== pInfo ========
        {
            pInfo.setLayout(new GridLayoutManager(1, 5, new Insets(0, 0, 0, 0), -1, -1));
            pInfo.add(lThumbnail, new GridConstraints(0, 0, 1, 1,
                    GridConstraints.ANCHOR_WEST, GridConstraints.FILL_BOTH,
                    GridConstraints.SIZEPOLICY_CAN_SHRINK,
                    GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                    null, null, null));
            pInfo.add(cbValue, new GridConstraints(0, 1, 1, 1,
                    GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL,
                    GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                    GridConstraints.SIZEPOLICY_FIXED,
                    null, null, null));

            //---- lDuration ----
            lDuration.setText("00:00");
            pInfo.add(lDuration, new GridConstraints(0, 2, 1, 1,
                    GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_NONE,
                    GridConstraints.SIZEPOLICY_FIXED,
                    GridConstraints.SIZEPOLICY_FIXED,
                    null, null, null));
            pInfo.add(cbQuality, new GridConstraints(0, 3, 1, 1,
                    GridConstraints.ANCHOR_EAST, GridConstraints.FILL_NONE,
                    GridConstraints.SIZEPOLICY_CAN_SHRINK,
                    GridConstraints.SIZEPOLICY_FIXED,
                    null, null, null));
            pInfo.add(chbDownloadable, new GridConstraints(0, 4, 1, 1,
                    GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_NONE,
                    GridConstraints.SIZEPOLICY_FIXED,
                    GridConstraints.SIZEPOLICY_FIXED,
                    null, null, null));
        }
        add(pInfo, new GridConstraints(0, 0, 1, 1,
                GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH,
                GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                null, null, null));
        add(pDownloadProgress, new GridConstraints(1, 0, 1, 1,
                GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL,
                GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                null, null, null));
        // JFormDesigner - End of component initialization  //GEN-END:initComponents
    }

    // JFormDesigner - Variables declaration - DO NOT MODIFY  //GEN-BEGIN:variables
    private JPanel pInfo;
    private JLabel lThumbnail;
    private JComboBox cbValue;
    private JLabel lDuration;
    private JComboBox cbQuality;
    private JCheckBox chbDownloadable;
    private JProgressBar pDownloadProgress;
    // JFormDesigner - End of variables declaration  //GEN-END:variables
}
