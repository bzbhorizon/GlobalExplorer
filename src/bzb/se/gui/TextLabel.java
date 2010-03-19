package bzb.se.gui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;

import javax.swing.JLabel;

public class TextLabel extends JLabel {
	public TextLabel(String title, Font f) {
		super(title, null, JLabel.CENTER);
		
		this.setForeground(Color.WHITE);
		this.setFont(f);
		this.setHorizontalTextPosition(JLabel.CENTER);
		this.setMaximumSize(new Dimension(800, 600));
	}
}
