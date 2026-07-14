package com.bkk.spk.view;

import com.bkk.spk.dao.AdminDAO;
import com.bkk.spk.model.Admin;
import com.bkk.spk.util.Session;
import com.bkk.spk.view.util.ButtonStyle;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.net.URL;

/**
 * Dialog login modal. Buka sebelum MainFrame -> kalau batal/keluar, app stop.
 *
 * CATATAN KEAMANAN: password sekarang dibandingkan plaintext dengan yang di DB.
 * Komen di Admin.java menyebut "hash BCrypt" tapi AdminDAO.insert masih plaintext.
 * TODO kalau BCrypt sudah dipasang: ganti bagian validasi di bawah dengan
 *   BCrypt.checkpw(passwordInput, admin.getPassword()).
 */
public class LoginDialog extends JDialog {

    private boolean loginBerhasil = false;

    private JTextField txtUsername;
    private JPasswordField txtPassword;
    private JLabel lblError;

    public LoginDialog(Frame owner) {
        super(owner, "Login Admin — SPK Profile Matching BKK", true);
        initComponents();
    }

    /** True kalau user berhasil login, false kalu batal / tutup dialog. */
    public boolean isLoginBerhasil() {
        return loginBerhasil;
    }

    private void initComponents() {
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        //setPreferredSize(new Dimension(400, 320));
        setResizable(false);

        JPanel root = new JPanel(new GridBagLayout());
        root.setBackground(Color.WHITE);
        root.setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 30));
        setContentPane(root);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 0, 6, 0);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;

        // Logo
        JLabel lblLogo = new JLabel();
        lblLogo.setHorizontalAlignment(SwingUtilities.CENTER);
        URL logoUrl = getClass().getResource("/com/bkk/spk/resources/logo_smk.png");
        if (logoUrl != null) {
            ImageIcon raw = new ImageIcon(logoUrl);
            Image scaled = raw.getImage().getScaledInstance(90, 90, Image.SCALE_SMOOTH);
            lblLogo.setIcon(new ImageIcon(scaled));
        }
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        gbc.insets = new Insets(0, 0, 14, 0);
        root.add(lblLogo, gbc);

        // Judul
        JLabel lblTitle = new JLabel("Login");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 22));
        gbc.gridy = 1;
        gbc.insets = new Insets(0, 0, 4, 0);
        root.add(lblTitle, gbc);

        JLabel lblSubtitle = new JLabel("Sistem Pendukun Keputusan Profile Matching");
        lblSubtitle.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblSubtitle.setForeground(new Color(100, 100, 100));
        gbc.gridy = 2;
        gbc.insets = new Insets(0, 0, 18, 0);
        root.add(lblSubtitle, gbc);

        gbc.gridwidth = 1;
        gbc.insets = new Insets(6, 0, 2, 0);

        // Username
        JLabel lblUser = new JLabel("Username");
        lblUser.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        gbc.gridx = 0; gbc.gridy = 3;
        root.add(lblUser, gbc);

        txtUsername = new JTextField(20);
        txtUsername.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        gbc.gridx = 0; gbc.gridy = 4;
        root.add(txtUsername, gbc);

        // Password
        JLabel lblPass = new JLabel("Password");
        lblPass.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        gbc.gridx = 0; gbc.gridy = 5;
        root.add(lblPass, gbc);

        txtPassword = new JPasswordField(20);
        txtPassword.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        // Enter di field mana pun yang fokus langsung trigger login -> di-handle rootPane default button
        gbc.gridx = 0; gbc.gridy = 6;
        root.add(txtPassword, gbc);

        // Error label (awalnya kosong)
        lblError = new JLabel(" ");
        lblError.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblError.setForeground(new Color(200, 50, 50));
        gbc.gridx = 0; gbc.gridy = 7;
        root.add(lblError, gbc);

        // Tombol
        JButton btnLogin = new JButton("Login");
        ButtonStyle.primary(btnLogin);
        btnLogin.addActionListener(this::doLogin);
        gbc.gridx = 0; gbc.gridy = 8;
        gbc.insets = new Insets(12, 0, 0, 0);
        root.add(btnLogin, gbc);

        JButton btnBatal = new JButton("Keluar");
        ButtonStyle.secondary(btnBatal);
        btnBatal.addActionListener((ActionEvent e) -> {
            loginBerhasil = false;
            dispose();
        });
        gbc.gridx = 0; gbc.gridy = 9;
        gbc.insets = new Insets(2, 0, 15, 0);
        root.add(btnBatal, gbc);

        pack();
        setLocationRelativeTo(getOwner());
        getRootPane().setDefaultButton(btnLogin);
    }

    /** Validasi kredensial via AdminDAO. Kalau sukses, isi Session + tutup dialog. */
    private void doLogin(ActionEvent e) {
        String username = txtUsername.getText().trim();
        String password = new String(txtPassword.getPassword());

        if (username.isEmpty() || password.isEmpty()) {
            showError("Username dan password wajib diisi.");
            return;
        }

        Admin admin = new AdminDAO().getByUsername(username);
        // TODO: ganti perbandingan ini dengan BCrypt.checkpw(password, admin.getPassword())
        //       setelah hashing BCrypt dipasang di AdminDAO.insert.
        if (admin == null || !password.equals(admin.getPassword())) {
            showError("Username atau password salah.");
            txtPassword.setText("");
            return;
        }

        Session.setCurrentAdmin(admin);
        loginBerhasil = true;
        dispose();
    }

    private void showError(String pesan) {
        lblError.setText(pesan);
    }

    /** Convenience: buka dialog, blok sampai user selesai, kembalikan status login. */
    public static boolean tampilkanDanTunggu() {
        LoginDialog dialog = new LoginDialog(null);
        dialog.pack();
        dialog.setLocationRelativeTo(null);
        dialog.setVisible(true); // blocking (modal)
        return dialog.isLoginBerhasil();
    }
}
