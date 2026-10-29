import java.security.MessageDigest;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class BlockchainGUI {
    static class Block {
        int index;
        String data, timestamp, previousHash, hash;
        int nonce = 0;

        Block(int index, String data, String previousHash) {
            this.index = index;
            this.data = data;
            this.previousHash = previousHash;
            this.timestamp = Instant.now().toString();
            this.hash = calculateHash();
        }

        String calculateHash() {
            String input = index + data + timestamp + previousHash + nonce;
            try {
                MessageDigest md = MessageDigest.getInstance("SHA-256");
                byte[] bytes = md.digest(input.getBytes(StandardCharsets.UTF_8));
                StringBuilder hex = new StringBuilder();
                for (byte b : bytes) hex.append(String.format("%02x", b & 0xff));
                return hex.toString();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        void mineBlock(int difficulty) {
            String target = "0".repeat(difficulty);
            while (!hash.startsWith(target)) {
                nonce++;
                hash = calculateHash();
            }
        }
    }

    static class Blockchain {
        List<Block> chain = new ArrayList<>();
        int difficulty = 3;

        Blockchain() {
            Block genesis = new Block(0, "Genesis Block", "0");
            genesis.mineBlock(difficulty);
            chain.add(genesis);
        }

        void addBlock(String data) {
            Block previous = chain.get(chain.size() - 1);
            Block block = new Block(chain.size(), data, previous.hash);
            block.mineBlock(difficulty);
            chain.add(block);
        }

        boolean isValid() {
            for (int i = 0; i < chain.size(); i++) {
                Block current = chain.get(i);
                if (!current.hash.equals(current.calculateHash())) return false;
                if (!current.hash.startsWith("0".repeat(difficulty))) return false;
                if (i > 0) {
                    if (!current.previousHash.equals(chain.get(i - 1).hash)) return false;
                } else if (!current.previousHash.equals("0")) {
                    return false;
                }
            }
            return true;
        }
    }

    private final Blockchain blockchain = new Blockchain();
    private final JTextArea blocksArea = new JTextArea();
    private final JTextArea outputArea = new JTextArea();
    private final JTextField transactionField = new JTextField();
    private final JTextField tamperField = new JTextField();
    private final JLabel statusLabel = new JLabel();

    private final Color background = new Color(13, 25, 45);
    private final Color panelColor = new Color(23, 42, 68);
    private final Color blue = new Color(35, 130, 245);
    private final Color textColor = new Color(230, 240, 255);

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new BlockchainGUI().createUI());
    }

    private void createUI() {
        JFrame frame = new JFrame("Blockchain Demo | Java");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(1100, 760);
        frame.setMinimumSize(new Dimension(800, 600));
        frame.setLocationRelativeTo(null);

        JPanel root = new JPanel(new BorderLayout(12, 12));
        root.setBackground(background);
        root.setBorder(new EmptyBorder(16, 16, 16, 16));
        frame.setContentPane(root);

        JLabel heading = new JLabel("  BLOCKCHAIN DEMO");
        heading.setFont(new Font("SansSerif", Font.BOLD, 27));
        heading.setForeground(Color.WHITE);
        JLabel subtitle = new JLabel("  A visual demonstration of hashing, mining and data integrity");
        subtitle.setForeground(new Color(165, 195, 230));

        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        JPanel titles = new JPanel(new GridLayout(2, 1, 0, 4));
        titles.setOpaque(false);
        titles.add(heading);
        titles.add(subtitle);
        header.add(titles, BorderLayout.CENTER);
        root.add(header, BorderLayout.NORTH);

        JPanel left = new JPanel();
        left.setOpaque(false);
        left.setLayout(new BoxLayout(left, BoxLayout.Y_AXIS));
        left.setPreferredSize(new Dimension(290, 0));

        JPanel addPanel = makePanel("Add a transaction");
        transactionField.setToolTipText("Example: Alice sends Bob 10 coins");
        styleField(transactionField);
        JButton addButton = makeButton("Mine and Add Block", blue);
        addPanel.add(transactionField, BorderLayout.CENTER);
        addPanel.add(addButton, BorderLayout.SOUTH);
        addButton.addActionListener(e -> addTransaction());
        transactionField.addActionListener(e -> addTransaction());

        JPanel operations = makePanel("Blockchain operations");
        JButton validateButton = makeButton("Validate Blockchain", new Color(105, 75, 220));
        JButton resetButton = makeButton("Reset Blockchain", new Color(45, 160, 115));
        operations.add(validateButton, BorderLayout.CENTER);
        operations.add(resetButton, BorderLayout.SOUTH);
        validateButton.addActionListener(e -> validateChain());
        resetButton.addActionListener(e -> {
            blockchain.chain.clear();
            Block genesis = new Block(0, "Genesis Block", "0");
            genesis.mineBlock(blockchain.difficulty);
            blockchain.chain.add(genesis);
            log("Blockchain reset and Genesis Block mined.");
            refreshBlocks();
        });

        JPanel tamperPanel = makePanel("Test tampering");
        JLabel tamperHint = new JLabel("Change the data in Block 1:");
        tamperHint.setForeground(textColor);
        tamperField.setText("Changed transaction");
        styleField(tamperField);
        JButton tamperButton = makeButton("Tamper with Block 1", new Color(220, 65, 85));
        tamperPanel.add(tamperHint, BorderLayout.NORTH);
        tamperPanel.add(tamperField, BorderLayout.CENTER);
        tamperPanel.add(tamperButton, BorderLayout.SOUTH);
        tamperButton.addActionListener(e -> tamperBlock());

        left.add(addPanel);
        left.add(Box.createVerticalStrut(12));
        left.add(operations);
        left.add(Box.createVerticalStrut(12));
        left.add(tamperPanel);

        blocksArea.setEditable(false);
        blocksArea.setLineWrap(true);
        blocksArea.setWrapStyleWord(true);
        blocksArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 13));
        blocksArea.setBackground(new Color(9, 19, 35));
        blocksArea.setForeground(new Color(180, 220, 255));
        blocksArea.setBorder(new EmptyBorder(12, 12, 12, 12));

        JPanel chainPanel = makePanel("Blockchain");
        chainPanel.add(new JScrollPane(blocksArea), BorderLayout.CENTER);

        outputArea.setEditable(false);
        outputArea.setRows(7);
        outputArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        outputArea.setBackground(new Color(9, 19, 35));
        outputArea.setForeground(new Color(130, 235, 190));
        outputArea.setBorder(new EmptyBorder(8, 8, 8, 8));

        JPanel bottom = makePanel("Activity log");
        bottom.add(new JScrollPane(outputArea), BorderLayout.CENTER);
        statusLabel.setFont(new Font("SansSerif", Font.BOLD, 15));
        statusLabel.setForeground(new Color(70, 220, 160));
        bottom.add(statusLabel, BorderLayout.SOUTH);

        JPanel right = new JPanel(new BorderLayout(0, 12));
        right.setOpaque(false);
        right.add(chainPanel, BorderLayout.CENTER);
        right.add(bottom, BorderLayout.SOUTH);

        JPanel main = new JPanel(new BorderLayout(12, 0));
        main.setOpaque(false);
        main.add(left, BorderLayout.WEST);
        main.add(right, BorderLayout.CENTER);
        root.add(main, BorderLayout.CENTER);

        log("Blockchain started. Genesis Block created and mined.");
        refreshBlocks();
        frame.setVisible(true);
    }

    private JPanel makePanel(String title) {
        JPanel panel = new JPanel(new BorderLayout(8, 10));
        panel.setBackground(panelColor);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(48, 76, 112)),
                BorderFactory.createTitledBorder(
                        BorderFactory.createEmptyBorder(8, 8, 8, 8),
                        title, 0, 0, new Font("SansSerif", Font.BOLD, 15), Color.WHITE)));
        panel.setBorder(new EmptyBorder(12, 12, 12, 12));
        return panel;
    }

    private JButton makeButton(String label, Color color) {
        JButton button = new JButton(label);
        button.setFont(new Font("SansSerif", Font.BOLD, 13));
        button.setForeground(Color.WHITE);
        button.setBackground(color);
        button.setFocusPainted(false);
        button.setBorder(new EmptyBorder(11, 10, 11, 10));
        return button;
    }

    private void styleField(JTextField field) {
        field.setFont(new Font("SansSerif", Font.PLAIN, 14));
        field.setBackground(new Color(9, 19, 35));
        field.setForeground(Color.WHITE);
        field.setCaretColor(Color.WHITE);
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(65, 95, 135)),
                new EmptyBorder(10, 8, 10, 8)));
    }

    private void addTransaction() {
        String data = transactionField.getText().trim();
        if (data.isEmpty()) {
            JOptionPane.showMessageDialog(null, "Please enter transaction data.");
            return;
        }
        log("Mining a new block...");
        blockchain.addBlock(data);
        log("Block " + (blockchain.chain.size() - 1) + " mined and added.");
        transactionField.setText("");
        refreshBlocks();
    }

    private void validateChain() {
        boolean valid = blockchain.isValid();
        statusLabel.setText(valid ? "✓ Blockchain is VALID" : "✗ Blockchain is NOT VALID");
        statusLabel.setForeground(valid ? new Color(70, 220, 160) : new Color(255, 90, 110));
        log(valid ? "Validation passed: all blocks are valid." :
                "Validation failed: data, hashes, or links do not match.");
        refreshBlocks();
    }

    private void tamperBlock() {
        if (blockchain.chain.size() < 2) {
            JOptionPane.showMessageDialog(null, "Add a transaction block first.");
            return;
        }
        String changedData = tamperField.getText().trim();
        if (changedData.isEmpty()) {
            JOptionPane.showMessageDialog(null, "Enter the replacement data.");
            return;
        }
        blockchain.chain.get(1).data = changedData;
        log("Block 1 data was changed without recalculating its hash.");
        validateChain();
    }

    private void refreshBlocks() {
        StringBuilder view = new StringBuilder();
        for (Block b : blockchain.chain) {
            view.append("══════════════════════════════════════════════════\n");
            view.append("BLOCK ").append(b.index);
            if (b.index == 0) view.append(" — GENESIS BLOCK");
            view.append("\n");
            view.append("Data:          ").append(b.data).append("\n");
            view.append("Timestamp:     ").append(b.timestamp).append("\n");
            view.append("Previous hash: ").append(b.previousHash).append("\n");
            view.append("Hash:          ").append(b.hash).append("\n");
            view.append("Nonce:         ").append(b.nonce).append("\n\n");
        }
        blocksArea.setText(view.toString());
        blocksArea.setCaretPosition(0);
        boolean valid = blockchain.isValid();
        statusLabel.setText(valid ? "✓ Blockchain is VALID" : "✗ Blockchain is NOT VALID");
        statusLabel.setForeground(valid ? new Color(70, 220, 160) : new Color(255, 90, 110));
    }

    private void log(String message) {
        outputArea.append("• " + message + "\n");
        outputArea.setCaretPosition(outputArea.getDocument().getLength());
    }
}
