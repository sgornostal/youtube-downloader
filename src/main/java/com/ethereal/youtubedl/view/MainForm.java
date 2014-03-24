package com.ethereal.youtubedl.view;

import com.ethereal.youtubedl.Main;
import com.ethereal.youtubedl.utils.RuntimeUtils;
import com.ethereal.youtubedl.youtube.VideoSearch;
import com.ethereal.youtubedl.youtube.VideoSearchProvider;
import com.ethereal.youtubedl.youtube.YoutubeResult;
import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import org.apache.log4j.Logger;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Arrays;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * @author Slava
 */
public class MainForm extends JFrame {

    private static final Logger logger = Logger.getLogger("SearchYoutube");
    public static boolean YOUTUBE_DL_UPDATED;

    private Integer progress = 0;
    private java.util.List<String> trackList;
    private SwingWorker<Void, Void> worker;
    private boolean isInterrupted;


    private synchronized void incProgress() {
        progress++;
        pProgressBar.setValue((int) (((double) progress / trackList.size()) * 100));
    }

    public MainForm() {
        initComponents();
        setVisible(true);

        if (!YOUTUBE_DL_UPDATED) {
            taTrackList.setEnabled(false);
            bProcess.setEnabled(false);

            final JDialog waitDialog = new JDialog(this, true);
            new SwingWorker<String, Void>() {

                @Override
                protected String doInBackground() throws Exception {
                    return RuntimeUtils.updateYoutubeDl();
                }

                @Override
                protected void done() {
                    try {
                        JOptionPane.showMessageDialog(MainForm.this, get(), "Update Info", JOptionPane.INFORMATION_MESSAGE);
                        YOUTUBE_DL_UPDATED = true;
                    } catch (Exception e) {
                        JOptionPane.showMessageDialog(MainForm.this, e.toString(), "Update Error", JOptionPane.ERROR_MESSAGE);
                    }
                    taTrackList.setEnabled(true);
                    bProcess.setEnabled(true);
                    waitDialog.dispose();
                }
            }.execute();
            waitDialog.setUndecorated(true);
            JPanel panel = new JPanel();
            final JLabel label = new JLabel("Please wait... Updating youtube-dl");
            panel.add(label);
            waitDialog.add(panel);
            waitDialog.pack();
            waitDialog.setLocationRelativeTo(this);
            waitDialog.setVisible(true);
        }

        bProcess.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

                if (taTrackList.getText().isEmpty()) {
                    JOptionPane.showMessageDialog(MainForm.this, "Tracklist is empty", "Empty field", JOptionPane.WARNING_MESSAGE);
                    return;
                }

                isInterrupted = false;
                bProcess.setEnabled(false);
                taTrackList.setEditable(false);
                bCancel.setEnabled(true);
                final java.util.List<java.util.List<YoutubeResult>> results = new java.util.ArrayList<>();
                trackList = Arrays.asList(taTrackList.getText().split("\\n"));
                final VideoSearch videoSearch = new VideoSearchProvider();
                worker = new SwingWorker<Void, Void>() {
                    @Override
                    protected Void doInBackground() throws Exception {
                        final ExecutorService executorService = Executors.newCachedThreadPool();

                        for (final String track : trackList) {
                            executorService.submit(new Runnable() {
                                @Override
                                public void run() {
                                    results.add(videoSearch.searchResults(track));
                                    incProgress();
                                }
                            });
                        }
                        executorService.shutdown();
                        try {
                            executorService.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
                        } catch (InterruptedException e) {
                            logger.warn(e);
                            JOptionPane.showMessageDialog(MainForm.this, "Canceled", "Warning", JOptionPane.WARNING_MESSAGE);
                        }
                        return null;
                    }

                    @Override
                    protected void done() {
                        if (!isInterrupted) {
                            new TrackListProcessForm(results);
                            MainForm.this.dispose();
                        }
                    }
                };
                worker.execute();
            }
        });

        bCancel.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                bProcess.setEnabled(true);
                taTrackList.setEditable(true);
                bCancel.setEnabled(false);
                pProgressBar.setValue(0);
                isInterrupted = true;
                worker.cancel(true);
            }
        });

    }

    private void initComponents() {
        // JFormDesigner - Component initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents
        lTitle = new JLabel();
        spTextView = new JScrollPane();
        taTrackList = new JTextArea();
        panel1 = new JPanel();
        bProcess = new JButton();
        bCancel = new JButton();
        pProgressBar = new JProgressBar();

        //======== this ========
        setMinimumSize(new Dimension(370, 420));
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setTitle("Youtube Downloader");
        Container contentPane = getContentPane();
        contentPane.setLayout(new GridLayoutManager(3, 1, new Insets(5, 5, 5, 5), -1, -1));

        //---- lTitle ----
        lTitle.setText("Paste enter separated track list in form below ");
        contentPane.add(lTitle, new GridConstraints(0, 0, 1, 1,
                GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL,
                GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                GridConstraints.SIZEPOLICY_FIXED,
                null, null, null));

        //======== spTextView ========
        {
            spTextView.setViewportView(taTrackList);
        }
        contentPane.add(spTextView, new GridConstraints(1, 0, 1, 1,
                GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH,
                GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                null, null, null));

        //======== panel1 ========
        {
            panel1.setLayout(new GridLayoutManager(1, 3, new Insets(0, 0, 0, 0), -1, -1));

            //---- bProcess ----
            bProcess.setText("Process");
            panel1.add(bProcess, new GridConstraints(0, 0, 1, 1,
                    GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL,
                    GridConstraints.SIZEPOLICY_FIXED,
                    GridConstraints.SIZEPOLICY_FIXED,
                    null, null, null));

            //---- bCancel ----
            bCancel.setText("Cancel");
            bCancel.setEnabled(false);
            panel1.add(bCancel, new GridConstraints(0, 1, 1, 1,
                    GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL,
                    GridConstraints.SIZEPOLICY_FIXED,
                    GridConstraints.SIZEPOLICY_FIXED,
                    null, null, null));
            panel1.add(pProgressBar, new GridConstraints(0, 2, 1, 1,
                    GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL,
                    GridConstraints.SIZEPOLICY_FIXED,
                    GridConstraints.SIZEPOLICY_FIXED,
                    null, null, null));
        }
        contentPane.add(panel1, new GridConstraints(2, 0, 1, 1,
                GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH,
                GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                GridConstraints.SIZEPOLICY_FIXED,
                null, null, null));
        pack();
        setLocationRelativeTo(getOwner());
        // JFormDesigner - End of component initialization  //GEN-END:initComponents
    }

    // JFormDesigner - Variables declaration - DO NOT MODIFY  //GEN-BEGIN:variables
    private JLabel lTitle;
    private JScrollPane spTextView;
    private JTextArea taTrackList;
    private JPanel panel1;
    private JButton bProcess;
    private JButton bCancel;
    private JProgressBar pProgressBar;
    // JFormDesigner - End of variables declaration  //GEN-END:variables
}
