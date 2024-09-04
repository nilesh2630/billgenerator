
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;

public class MenuManagerApp1 extends JFrame {
     JTextField nameField;
     JTextField categoryField;
     JTextField priceField;
     JButton addButton;
     JButton updateButton;
     JTable itemsTable;
     DefaultTableModel tableModel;
     Connection connection;

    public MenuManagerApp1() {
        // super("Menu Manager");
        initComponents();
        setupDatabase();
        loadItems();
    }

    public  void initComponents() {
        nameField = new JTextField();
        categoryField = new JTextField();
        priceField = new JTextField();
        addButton = new JButton("Add Item");
        updateButton = new JButton("Update Price");
        
        tableModel = new DefaultTableModel(new Object[]{"Name", "Category", "Price"}, 0);
        itemsTable = new JTable(tableModel);

        JScrollPane scrollPane = new JScrollPane(itemsTable);

        JPanel inputPanel = new JPanel(new GridLayout(3, 2, 5, 5));
        inputPanel.add(new JLabel("Name:"));
        inputPanel.add(nameField);
        inputPanel.add(new JLabel("Category:"));
        inputPanel.add(categoryField);
        inputPanel.add(new JLabel("Price:"));
        inputPanel.add(priceField);

        JPanel buttonPanel = new JPanel(new FlowLayout());
        buttonPanel.add(addButton);
        buttonPanel.add(updateButton);
        

        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.add(inputPanel, BorderLayout.NORTH);
        mainPanel.add(buttonPanel, BorderLayout.CENTER);

        getContentPane().add(mainPanel, BorderLayout.NORTH);
        getContentPane().add(scrollPane, BorderLayout.CENTER);

        addButton.addActionListener(e -> addItem());
        updateButton.addActionListener(e -> updateItem());
        

        itemsTable.getSelectionModel().addListSelectionListener(e -> {
            int selectedRow = itemsTable.getSelectedRow();
            if (selectedRow != -1) {
                String name = (String) tableModel.getValueAt(selectedRow, 0);
                String category = (String) tableModel.getValueAt(selectedRow, 1);
                double price = (double) tableModel.getValueAt(selectedRow, 2);
                nameField.setText(name);
                categoryField.setText(category);
                priceField.setText(String.valueOf(price));
            }
        });
    }

    public void setupDatabase() {
        try {
            connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/restaurant", "root", "sanjay s3");
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Database connection failed!");
            System.exit(1);
        }
    }

    public void loadItems() {
        try {
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery("SELECT name, category, price FROM items");
            while (resultSet.next()) {
                String name = resultSet.getString("name");
                String category = resultSet.getString("category");
                double price = resultSet.getDouble("price");
                tableModel.addRow(new Object[]{name, category, price});
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Failed to load items from database.");
        }
    }

    public void addItem() {
        String name = nameField.getText();
        String category = categoryField.getText();
        double price = Double.parseDouble(priceField.getText());

        try {
            String insertItemSQL = "INSERT INTO items (name, category, price) VALUES (?, ?, ?)";
            PreparedStatement statement = connection.prepareStatement(insertItemSQL);
            statement.setString(1, name);
            statement.setString(2, category);
            statement.setDouble(3, price);
            statement.executeUpdate();

            JOptionPane.showMessageDialog(null, "Item added successfully!");
            tableModel.addRow(new Object[]{name, category, price});
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(null, "Failed to add item. Please try again.");
        }
    }

    private void updateItem() {
        int selectedRow = itemsTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(null, "Please select an item to update.");
            return;
        }

        String name = nameField.getText();
        String category = categoryField.getText();
        double price = Double.parseDouble(priceField.getText());

        try {
            String updateItemSQL = "UPDATE items SET category = ?, price = ? WHERE name = ?";
            PreparedStatement statement = connection.prepareStatement(updateItemSQL);
            statement.setString(1, category);
            statement.setDouble(2, price);
            statement.setString(3, name);
            int rowsUpdated = statement.executeUpdate();

            if (rowsUpdated > 0) {
                JOptionPane.showMessageDialog(null, "Item updated successfully!");
                tableModel.setValueAt(category, selectedRow, 1);
                tableModel.setValueAt(price, selectedRow, 2);
            } else {
                JOptionPane.showMessageDialog(null, "Item not found.");
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(null, "Failed to update item. Please try again.");
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            MenuManagerApp1 app = new MenuManagerApp1();
            Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
            app.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            app.setSize(screenSize);
            app.setVisible(true);
        });
    }
}
