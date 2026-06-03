package com.personalllm;

import com.personalllm.ui.ChatWindow;
import javax.swing.SwingUtilities;

public class Main {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(ChatWindow::new);
    }
}
