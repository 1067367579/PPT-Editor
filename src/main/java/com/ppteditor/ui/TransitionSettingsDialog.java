package com.ppteditor.ui;

import com.ppteditor.core.enums.AnimationType;
import com.ppteditor.core.model.Presentation;

import javax.swing.*;
import java.awt.*;

public class TransitionSettingsDialog extends JDialog {

    private JComboBox<AnimationType> animationTypeCombo;
    private JSpinner durationSpinner;
    private Presentation presentation;

    public TransitionSettingsDialog(Frame owner, Presentation presentation) {
        super(owner, "设置全局过渡动画", true);
        this.presentation = presentation;
        
        setLayout(new BorderLayout(10, 10));
        
        // --- Main Panel ---
        JPanel mainPanel = new JPanel(new GridBagLayout());
        mainPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Animation Type
        gbc.gridx = 0;
        gbc.gridy = 0;
        mainPanel.add(new JLabel("动画类型:"), gbc);

        gbc.gridx = 1;
        animationTypeCombo = new JComboBox<>(AnimationType.values());
        animationTypeCombo.setSelectedItem(presentation.getTransitionAnimation());
        mainPanel.add(animationTypeCombo, gbc);

        // Animation Duration
        gbc.gridx = 0;
        gbc.gridy = 1;
        mainPanel.add(new JLabel("持续时间 (ms):"), gbc);

        gbc.gridx = 1;
        durationSpinner = new JSpinner(new SpinnerNumberModel(presentation.getTransitionDuration(), 100, 5000, 100));
        mainPanel.add(durationSpinner, gbc);
        
        add(mainPanel, BorderLayout.CENTER);

        // --- Button Panel ---
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton okButton = new JButton("确认");
        JButton cancelButton = new JButton("取消");

        okButton.addActionListener(e -> onOK());
        cancelButton.addActionListener(e -> onCancel());

        buttonPanel.add(okButton);
        buttonPanel.add(cancelButton);
        
        add(buttonPanel, BorderLayout.SOUTH);

        pack();
        setLocationRelativeTo(owner);
    }

    private void onOK() {
        presentation.setTransitionAnimation((AnimationType) animationTypeCombo.getSelectedItem());
        presentation.setTransitionDuration((Integer) durationSpinner.getValue());
        setVisible(false);
    }

    private void onCancel() {
        setVisible(false);
    }
} 