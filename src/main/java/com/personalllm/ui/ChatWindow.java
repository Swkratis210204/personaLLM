package com.personalllm.ui;

import com.personalllm.client.OllamaClient;
import com.personalllm.model.Message;

import javax.swing.*;
import javax.swing.text.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class ChatWindow extends JFrame {

    private static final String MODEL           = "gemma3:4b";
    private static final Color  USER_COLOR      = new Color(0x1a5276);
    private static final Color  ASSISTANT_COLOR = new Color(0x1e6630);
    private static final Color  ERROR_COLOR     = new Color(0xc0392b);
    private static final Color  STATUS_COLOR    = new Color(0x888888);
    private static final Font   CHAT_FONT       = new Font(Font.SANS_SERIF, Font.PLAIN, 14);

    private final OllamaClient  client  = new OllamaClient();
    private final List<Message> history = new ArrayList<>();

    private JTextPane  chatPane;
    private JTextField inputField;
    private JButton    sendButton;
    private JTextField systemPromptField;
    private JLabel     statusBar;
    private int        thinkingPos = -1;

    public ChatWindow() {
        super("personaLM");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(860, 640);
        setMinimumSize(new Dimension(520, 400));
        setLocationRelativeTo(null);
        buildUI();
        startOllama();
        setVisible(true);
    }

    // ── UI construction ───────────────────────────────────────────────────────

    private void buildUI() {
        setLayout(new BorderLayout());
        add(buildTopBar(),    BorderLayout.NORTH);
        add(buildChatArea(),  BorderLayout.CENTER);
        add(buildBottomPanel(), BorderLayout.SOUTH);
    }

    private JPanel buildTopBar() {
        JPanel bar = new JPanel(new BorderLayout(8, 0));
        bar.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, Color.LIGHT_GRAY),
                BorderFactory.createEmptyBorder(8, 10, 8, 10)));

        JLabel modelLabel = new JLabel("Model: " + MODEL);
        modelLabel.setForeground(new Color(0x555555));

        JPanel right = new JPanel(new BorderLayout(6, 0));
        right.add(new JLabel("System prompt:"), BorderLayout.WEST);
        systemPromptField = new JTextField("You are Anastasia, a helpful assistant.");
        right.add(systemPromptField, BorderLayout.CENTER);

        bar.add(modelLabel, BorderLayout.WEST);
        bar.add(right,      BorderLayout.CENTER);
        return bar;
    }

    private JScrollPane buildChatArea() {
        chatPane = new JTextPane();
        chatPane.setEditable(false);
        chatPane.setFont(CHAT_FONT);
        chatPane.setMargin(new Insets(10, 12, 10, 12));
        chatPane.setBackground(Color.WHITE);

        JScrollPane scroll = new JScrollPane(chatPane);
        scroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        return scroll;
    }

    // Bottom panel = input row + status bar
    private JPanel buildBottomPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.add(buildInputRow(), BorderLayout.CENTER);
        panel.add(buildStatusBar(), BorderLayout.SOUTH);
        return panel;
    }

    private JPanel buildInputRow() {
        JPanel bar = new JPanel(new BorderLayout(8, 0));
        bar.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, Color.LIGHT_GRAY),
                BorderFactory.createEmptyBorder(8, 10, 8, 10)));

        JButton clearButton = new JButton("Clear History");
        clearButton.addActionListener(e -> clearChat());

        inputField = new JTextField();
        inputField.setFont(CHAT_FONT);
        inputField.addActionListener(e -> sendMessage());

        sendButton = new JButton("Send  ▶");
        sendButton.setEnabled(false);
        sendButton.addActionListener(e -> sendMessage());

        bar.add(clearButton, BorderLayout.WEST);
        bar.add(inputField,  BorderLayout.CENTER);
        bar.add(sendButton,  BorderLayout.EAST);
        return bar;
    }

    private JLabel buildStatusBar() {
        statusBar = new JLabel("Starting...");
        statusBar.setFont(new Font(Font.SANS_SERIF, Font.ITALIC, 11));
        statusBar.setForeground(STATUS_COLOR);
        statusBar.setBorder(BorderFactory.createEmptyBorder(2, 12, 4, 10));
        return statusBar;
    }

    // ── Ollama startup ────────────────────────────────────────────────────────

    private void startOllama() {
        setStatus("Starting Ollama...", false);
        sendButton.setEnabled(false);

        new Thread(() -> {
            if (!client.isRunning()) {
                boolean started = client.startOllama();
                if (!started) {
                    SwingUtilities.invokeLater(() -> {
                        appendStatus("Ollama not found. Please install it — see SETUP.md\n\n");
                        setStatus("Ollama not found — see SETUP.md", true);
                    });
                    return;
                }
            }

            SwingUtilities.invokeLater(() -> {
                sendButton.setEnabled(true);
                inputField.requestFocusInWindow();
                setStatus("Ready", false);
            });
        }).start();
    }

    // ── Sending messages ──────────────────────────────────────────────────────

    private void sendMessage() {
        String text = inputField.getText().trim();
        if (text.isEmpty()) return;

        inputField.setText("");
        inputField.requestFocusInWindow();
        setSending(true);

        history.add(new Message(Message.Role.USER, text));
        appendSpeaker("You", USER_COLOR);
        appendText(text + "\n\n", USER_COLOR, false);
        appendSpeaker("Anastasia", ASSISTANT_COLOR);
        thinkingPos = chatPane.getDocument().getLength();
        appendStatus("(thinking... first response may take up to 30s while the model loads)");

        StringBuilder fullResponse = new StringBuilder();

        new Thread(() -> client.streamChat(
                MODEL,
                systemPromptField.getText(),
                history,
                token -> {
                    fullResponse.append(token);
                    SwingUtilities.invokeLater(() -> {
                        if (thinkingPos >= 0) {
                            try {
                                chatPane.getDocument().remove(thinkingPos,
                                        chatPane.getDocument().getLength() - thinkingPos);
                            } catch (BadLocationException ignored) {}
                            thinkingPos = -1;
                        }
                        appendText(token, ASSISTANT_COLOR, false);
                    });
                },
                () -> {
                    history.add(new Message(Message.Role.ASSISTANT, fullResponse.toString()));
                    SwingUtilities.invokeLater(() -> {
                        appendText("\n\n", ASSISTANT_COLOR, false);
                        setSending(false);
                        setStatus("Ready", false);
                    });
                },
                error -> SwingUtilities.invokeLater(() -> {
                    appendText(error + "\n\n", ERROR_COLOR, true);
                    setSending(false);
                    setStatus(error, true);
                })
        )).start();
    }

    // ── Chat display helpers ──────────────────────────────────────────────────

    private void appendSpeaker(String name, Color color) {
        SimpleAttributeSet style = new SimpleAttributeSet();
        StyleConstants.setForeground(style, color);
        StyleConstants.setBold(style, true);
        StyleConstants.setFontSize(style, 13);
        insert(name + ":\n", style);
    }

    private void appendText(String text, Color color, boolean italic) {
        SimpleAttributeSet style = new SimpleAttributeSet();
        StyleConstants.setForeground(style, color);
        StyleConstants.setItalic(style, italic);
        StyleConstants.setFontSize(style, 14);
        insert(text, style);
    }

    private void appendStatus(String text) {
        SimpleAttributeSet style = new SimpleAttributeSet();
        StyleConstants.setForeground(style, STATUS_COLOR);
        StyleConstants.setItalic(style, true);
        StyleConstants.setFontSize(style, 13);
        insert(text, style);
    }

    private void insert(String text, AttributeSet style) {
        StyledDocument doc = chatPane.getStyledDocument();
        try {
            doc.insertString(doc.getLength(), text, style);
        } catch (BadLocationException ignored) {}
        chatPane.setCaretPosition(doc.getLength());
    }

    // ── State helpers ─────────────────────────────────────────────────────────

    private void setStatus(String text, boolean error) {
        statusBar.setText(text);
        statusBar.setForeground(error ? ERROR_COLOR : STATUS_COLOR);
    }

    private void setSending(boolean sending) {
        sendButton.setEnabled(!sending);
        if (sending) setStatus("Anastasia is thinking...", false);
        inputField.requestFocusInWindow();
    }

    private void clearChat() {
        history.clear();
        chatPane.setText("");
        inputField.requestFocusInWindow();
        setStatus("Ready", false);
    }
}
