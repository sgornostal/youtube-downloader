package com.ethereal.youtubedl.view;

import com.ethereal.youtubedl.utils.RuntimeUtils;
import com.ethereal.youtubedl.youtube.YoutubeResult;
import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import org.apache.log4j.Logger;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * @author Slava
 */
public class TrackListProcessForm extends JFrame {

    private static final Logger logger = Logger.getLogger(TrackListProcessForm.class);

    private final java.util.List<YoutubeResultView> resultViewList = new ArrayList<>();
    private final ExecutorService executorService = Executors.newCachedThreadPool();
    private ExecutorService processExecutor;
    private SwingWorker<Void, Void> downloadWorker;

    public TrackListProcessForm(final java.util.List<java.util.List<YoutubeResult>> results) {
        initComponents();
        setVisible(true);
        spItemsView.getVerticalScrollBar().setUnitIncrement(16);
        final JDialog waitDialog = new JDialog(this, true);
        final SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {

                for (final java.util.List<YoutubeResult> youtubeResultList : results) {
                    executorService.submit(new Runnable() {
                        @Override
                        public void run() {
                            final YoutubeResultView youtubeResultView = new YoutubeResultView(youtubeResultList);
                            resultViewList.add(youtubeResultView);
                            pItems.add(youtubeResultView);
                            pItems.revalidate();
                            TrackListProcessForm.this.revalidate();
                        }
                    });
                }
                executorService.shutdown();
                try {
                    executorService.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
                } catch (InterruptedException e) {
                    logger.error(e);
                    JOptionPane.showMessageDialog(TrackListProcessForm.this, e.toString(), "Error", JOptionPane.ERROR_MESSAGE);
                }
                return null;
            }

            @Override
            protected void done() {
                waitDialog.dispose();
            }
        };
        worker.execute();
        waitDialog.setUndecorated(true);
        final JPanel panel = new JPanel();
        final JLabel label = new JLabel("Please wait...");
        panel.add(label);
        waitDialog.add(panel);
        waitDialog.pack();
        waitDialog.setLocationRelativeTo(this);
        waitDialog.setVisible(true);

        chbDownloadAll.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                final boolean downloadable = chbDownloadAll.isSelected();
                for (YoutubeResultView youtubeResultView : resultViewList) {
                    youtubeResultView.setDownloadable(downloadable);
                }
            }
        });
        chbDownloadAll.setSelected(true);

        bBack.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                new MainForm();
                TrackListProcessForm.this.dispose();
            }
        });

        bDownload.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (tfDirectory.getText().isEmpty()) {
                    JOptionPane.showMessageDialog(TrackListProcessForm.this,
                            "Please enter download directory", "Warning", JOptionPane.WARNING_MESSAGE);
                    return;
                }
                final String targetDirectory = tfDirectory.getText();
                final File f = new File(targetDirectory);
                if (!f.exists()) {
                    f.mkdir();
                }
                if (!f.exists() || !f.isDirectory()) {
                    JOptionPane.showMessageDialog(TrackListProcessForm.this,
                            "Please set up valid directory", "Warning", JOptionPane.WARNING_MESSAGE);
                    return;
                }

                final StringBuffer exceptionHandleBuffer = new StringBuffer();
                downloadWorker = new SwingWorker<Void, Void>() {
                    @Override
                    protected Void doInBackground() throws Exception {
                        for (final YoutubeResultView youtubeResultView : resultViewList) {
                            final YoutubeResult selectedResult = youtubeResultView.getSelectedYoutubeResult();
                            if (selectedResult != null) {
                                final StringBuilder paramBuilder = new StringBuilder(" ");
                                switch ((String) cbMediaType.getSelectedItem()) {
                                    case "VIDEO":
                                        paramBuilder.append("-f ");
                                        paramBuilder.append(youtubeResultView.getSelectedQuality().getFormatCode());
                                        paramBuilder.append(" ");
                                        break;
                                    case "AUDIO":
                                        paramBuilder.append("-x ");
                                        paramBuilder.append("--audio-quality 0 ");
                                        paramBuilder.append("--audio-format mp3");
                                        paramBuilder.append(" ");
                                        break;
                                }
                                paramBuilder.append("-o ").append(targetDirectory)
                                        .append(RuntimeUtils.getOSSeparator()).append("%(title)s.%(ext)s ");
                                processExecutor = Executors.newCachedThreadPool();
                                processExecutor.submit(new Runnable() {
                                    @Override
                                    public void run() {
                                        try {
                                            logger.info(String.format("Trying download video [%s] with params: %s",
                                                    selectedResult.getVideoId(), paramBuilder.toString()));

                                            RuntimeUtils.download(selectedResult.getVideoId(), paramBuilder.toString(), new RuntimeUtils.DownloadProgressListener() {
                                                @Override
                                                public void onChange(int progress) {
                                                    youtubeResultView.changeDownloadProgress(progress);
                                                }
                                            });
                                        } catch (InterruptedException ie) {
                                            logger.warn(ie.toString());
                                        } catch (Exception ex) {
                                            exceptionHandleBuffer.append(ex.toString());
                                            exceptionHandleBuffer.append("\n");
                                            logger.error("Video download fail", ex);
                                        }
                                    }
                                });
                            }
                        }
                        enableComponents(pTitle, false);
                        enableComponents(pItems, false);
                        enableComponents(pSettings, false);
                        bDownload.setEnabled(false);
                        bCancel.setEnabled(true);
                        bBack.setEnabled(false);
                        processExecutor.shutdown();
                        try {
                            processExecutor.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
                        } catch (InterruptedException e) {
                            logger.warn(e);
                            JOptionPane.showMessageDialog(TrackListProcessForm.this, "Canceled", "Warning", JOptionPane.WARNING_MESSAGE);
                        }
                        return null;
                    }

                    @Override
                    protected void done() {
                        if (!exceptionHandleBuffer.toString().isEmpty()) {
                            JOptionPane.showMessageDialog(TrackListProcessForm.this, exceptionHandleBuffer.toString(),
                                    "Warning", JOptionPane.WARNING_MESSAGE);
                        }
                        enableComponents(pTitle, true);
                        enableComponents(pItems, true);
                        enableComponents(pSettings, true);
                        bDownload.setEnabled(true);
                        bCancel.setEnabled(false);
                        bBack.setEnabled(true);

                        //wait before finish all threads
                        Executors.newSingleThreadScheduledExecutor().schedule(new Runnable() {
                            @Override
                            public void run() {
                                for (YoutubeResultView youtubeResultView : resultViewList) {
                                    youtubeResultView.clearProgress();
                                }
                                RuntimeUtils.clearCache(targetDirectory);
                            }
                        }, 1, TimeUnit.SECONDS);
                    }
                };
                downloadWorker.execute();
            }
        });

        bCancel.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int result = JOptionPane.showConfirmDialog(TrackListProcessForm.this, "Cancel download?", "Warning",
                        JOptionPane.OK_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE);
                if (result == JOptionPane.OK_OPTION) {
                    processExecutor.shutdownNow();
                    downloadWorker.cancel(true);
                }
            }
        });

        bEnterDirectory.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                final JFileChooser chooser = new JFileChooser();
                chooser.setCurrentDirectory(new java.io.File("."));
                chooser.setDialogTitle("Select destination folder");
                chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
                chooser.setAcceptAllFileFilterUsed(false);
                if (chooser.showDialog(TrackListProcessForm.this, "OK")
                        == JFileChooser.APPROVE_OPTION) {
                    tfDirectory.setText(chooser.getSelectedFile().getAbsolutePath());
                }
            }
        });

    }

    private void enableComponents(Container container, boolean enable) {
        Component[] components = container.getComponents();
        for (Component component : components) {
            component.setEnabled(enable);
            if (component instanceof Container) {
                enableComponents((Container) component, enable);
            }
        }
    }


    private void initComponents() {
        // JFormDesigner - Component initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents
        pTitle = new JPanel();
        lThumbnail = new JLabel();
        lSource = new JLabel();
        lDuration = new JLabel();
        lQuality = new JLabel();
        chbDownloadAll = new JCheckBox();
        spItemsView = new JScrollPane();
        pItems = new JPanel();
        pSettings = new JPanel();
        lMediaType = new JLabel();
        cbMediaType = new JComboBox<>();
        lTargetDir = new JLabel();
        pDir = new JPanel();
        tfDirectory = new JTextField();
        bEnterDirectory = new JButton();
        pProcess = new JPanel();
        bDownload = new JButton();
        bCancel = new JButton();
        bBack = new JButton();

        //======== this ========
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setTitle("Process Download");
        setMinimumSize(new Dimension(800, 400));
        Container contentPane = getContentPane();
        contentPane.setLayout(new GridLayoutManager(4, 1, new Insets(5, 5, 5, 5), -1, -1));

        //======== pTitle ========
        {
            pTitle.setLayout(new GridLayoutManager(1, 5, new Insets(0, 0, 0, 0), -1, -1));

            //---- lThumbnail ----
            lThumbnail.setText("Thumbnail");
            pTitle.add(lThumbnail, new GridConstraints(0, 0, 1, 1,
                    GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE,
                    GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                    GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                    null, null, null));

            //---- lSource ----
            lSource.setText("Source");
            pTitle.add(lSource, new GridConstraints(0, 1, 1, 1,
                    GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_NONE,
                    GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                    GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                    null, null, null));

            //---- lDuration ----
            lDuration.setText("Duration");
            pTitle.add(lDuration, new GridConstraints(0, 2, 1, 1,
                    GridConstraints.ANCHOR_EAST, GridConstraints.FILL_NONE,
                    GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                    GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                    null, null, null));

            //---- lQuality ----
            lQuality.setText("Quality");
            pTitle.add(lQuality, new GridConstraints(0, 3, 1, 1,
                    GridConstraints.ANCHOR_EAST, GridConstraints.FILL_NONE,
                    GridConstraints.SIZEPOLICY_FIXED,
                    GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                    null, null, null));

            //---- chbDownloadAll ----
            chbDownloadAll.setText("Download");
            pTitle.add(chbDownloadAll, new GridConstraints(0, 4, 1, 1,
                    GridConstraints.ANCHOR_EAST, GridConstraints.FILL_NONE,
                    GridConstraints.SIZEPOLICY_FIXED,
                    GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                    null, null, null, 0, true));
        }
        contentPane.add(pTitle, new GridConstraints(0, 0, 1, 1,
                GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL,
                GridConstraints.SIZEPOLICY_FIXED,
                GridConstraints.SIZEPOLICY_FIXED,
                null, null, null));

        //======== spItemsView ========
        {

            //======== pItems ========
            {
                pItems.setLayout(new BoxLayout(pItems, BoxLayout.Y_AXIS));
            }
            spItemsView.setViewportView(pItems);
        }
        contentPane.add(spItemsView, new GridConstraints(1, 0, 1, 1,
                GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH,
                GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                null, null, null));

        //======== pSettings ========
        {
            pSettings.setLayout(new GridLayoutManager(2, 2, new Insets(0, 0, 0, 0), -1, -1));

            //---- lMediaType ----
            lMediaType.setText("Media Type");
            pSettings.add(lMediaType, new GridConstraints(0, 0, 1, 1,
                    GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_NONE,
                    GridConstraints.SIZEPOLICY_FIXED,
                    GridConstraints.SIZEPOLICY_FIXED,
                    null, null, null));

            //---- cbMediaType ----
            cbMediaType.setModel(new DefaultComboBoxModel<>(new String[]{
                    "VIDEO",
                    "AUDIO"
            }));
            pSettings.add(cbMediaType, new GridConstraints(0, 1, 1, 1,
                    GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE,
                    GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                    GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                    null, null, null));

            //---- lTargetDir ----
            lTargetDir.setText("Target Directory");
            pSettings.add(lTargetDir, new GridConstraints(1, 0, 1, 1,
                    GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_NONE,
                    GridConstraints.SIZEPOLICY_FIXED,
                    GridConstraints.SIZEPOLICY_FIXED,
                    null, null, null));

            //======== pDir ========
            {
                pDir.setLayout(new GridLayoutManager(1, 2, new Insets(0, 0, 0, 0), -1, -1));
                pDir.add(tfDirectory, new GridConstraints(0, 0, 1, 1,
                        GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL,
                        GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                        GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                        null, null, null));

                //---- bEnterDirectory ----
                bEnterDirectory.setText("...");
                pDir.add(bEnterDirectory, new GridConstraints(0, 1, 1, 1,
                        GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_NONE,
                        GridConstraints.SIZEPOLICY_FIXED,
                        GridConstraints.SIZEPOLICY_FIXED,
                        null, null, null));
            }
            pSettings.add(pDir, new GridConstraints(1, 1, 1, 1,
                    GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH,
                    GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                    GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                    null, null, null));
        }
        contentPane.add(pSettings, new GridConstraints(2, 0, 1, 1,
                GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH,
                GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                GridConstraints.SIZEPOLICY_FIXED,
                null, null, null));

        //======== pProcess ========
        {
            pProcess.setLayout(new GridLayoutManager(1, 4, new Insets(0, 0, 0, 0), -1, -1));

            //---- bDownload ----
            bDownload.setText("Download");
            pProcess.add(bDownload, new GridConstraints(0, 0, 1, 1,
                    GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE,
                    GridConstraints.SIZEPOLICY_FIXED,
                    GridConstraints.SIZEPOLICY_FIXED,
                    null, null, null));

            //---- bCancel ----
            bCancel.setText("Cancel");
            bCancel.setEnabled(false);
            pProcess.add(bCancel, new GridConstraints(0, 1, 1, 1,
                    GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_NONE,
                    GridConstraints.SIZEPOLICY_FIXED,
                    GridConstraints.SIZEPOLICY_FIXED,
                    null, null, null));

            //---- bBack ----
            bBack.setText("Back");
            pProcess.add(bBack, new GridConstraints(0, 3, 1, 1,
                    GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_NONE,
                    GridConstraints.SIZEPOLICY_FIXED,
                    GridConstraints.SIZEPOLICY_FIXED,
                    null, null, null));
        }
        contentPane.add(pProcess, new GridConstraints(3, 0, 1, 1,
                GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE,
                GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                GridConstraints.SIZEPOLICY_FIXED,
                null, null, null));
        pack();
        setLocationRelativeTo(getOwner());
        // JFormDesigner - End of component initialization  //GEN-END:initComponents
    }

    // JFormDesigner - Variables declaration - DO NOT MODIFY  //GEN-BEGIN:variables
    private JPanel pTitle;
    private JLabel lThumbnail;
    private JLabel lSource;
    private JLabel lDuration;
    private JLabel lQuality;
    private JCheckBox chbDownloadAll;
    private JScrollPane spItemsView;
    private JPanel pItems;
    private JPanel pSettings;
    private JLabel lMediaType;
    private JComboBox<String> cbMediaType;
    private JLabel lTargetDir;
    private JPanel pDir;
    private JTextField tfDirectory;
    private JButton bEnterDirectory;
    private JPanel pProcess;
    private JButton bDownload;
    private JButton bCancel;
    private JButton bBack;
    // JFormDesigner - End of variables declaration  //GEN-END:variables
}
