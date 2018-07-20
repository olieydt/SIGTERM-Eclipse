import java.awt.AWTException;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;

public class Killer {
	
	private static JLabel label;
	private static JButton button;
	private static JList<String> processList;
	private static JScrollPane processScrollPane;
	private static DefaultListModel<String> processesModel = new DefaultListModel<>();
	
	public static void main(String args[]) {
		try {
			createJFrame();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private static void createJFrame() throws IOException {
		JFrame frame = new JFrame("Java Killer");
		frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		frame.addWindowListener(new java.awt.event.WindowAdapter() {
		    @Override
		    public void windowClosing(java.awt.event.WindowEvent windowEvent) {
		    	//print in window
		    	System.exit(0);
		    }
		});
		//add buttons
		frame.getContentPane().setLayout(new FlowLayout());
		JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
		label = new JLabel("No Java process selected.\n");
		panel.add(label);
		//get processes
		Timer timer = new Timer();
		timer.scheduleAtFixedRate(new TimerTask() {
			  @Override
			  public void run() {
			    try {
					updateProcesses();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			  }
			}, 0, 400);
		processList = new JList<String>(processesModel);
		processList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		processList.setLayoutOrientation(JList.VERTICAL);
		processScrollPane = new JScrollPane(processList);
		processScrollPane.setPreferredSize(new Dimension(300, 100));
		button = new JButton("SIGTERM");
		button.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				List<String> selectedList = processList.getSelectedValuesList();
				try {
					killJava(selectedList);
					processList.clearSelection();
					//refresh JList
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		});
		panel.add(button);
		frame.getContentPane().add(panel);
		frame.getContentPane().add(processScrollPane);
		frame.pack();
		frame.setVisible(true);
	}
	
	public static void updateProcesses() throws IOException {
		String cmd[] = new String[3];
		//make sure os is linux or mac
		if(isOSX() || isUnix()) {
			cmd[0] = "/bin/sh";
			cmd[1] = "-c";
			cmd[2] = "ps -ax | grep java";	
		}
		Process p = Runtime.getRuntime().exec(cmd);
		BufferedReader stdInput = new BufferedReader(new 
			     InputStreamReader(p.getInputStream()));
		List<String> processes = new ArrayList<>();
		String output = stdInput.readLine();
		while(output != null) {
			if(output.contains("-classpath")) {
				processes.add(output.split("-classpath")[1]);
			}
			output = stdInput.readLine();
		}
		for(String newProcess:processes) {
			if(!processesModel.contains(newProcess)) {
				processesModel.addElement(newProcess);
			}
		}
		for(int i=processesModel.size()-1; i>=0; i--) {
			if(!processes.contains(processesModel.getElementAt(i))) {
				processesModel.removeElementAt(i);
			}
		}
		
		/*if(processes.size() > 0) {
			String[] processesStr = new String[processes.size()];
			return processes.toArray(processesStr);
		}
		return null;*/
	}
	
	public static void killJava(List<String> selectedList) throws IOException {
		for(String process:selectedList) {
			executeKill(process);
		}
	}
	
	private static void executeKill(String process) throws IOException {
		String cmd[] = new String[3];
		//make sure os is linux or mac
		if(isOSX() || isUnix()) {
			cmd[0] = "/bin/sh";
			cmd[1] = "-c";
			cmd[2] = "pkill -f '" + process + "'";	
		}
		Runtime.getRuntime().exec(cmd);
	}

	public static boolean isOSX() {
		return System.getProperty("os.name").toLowerCase().equals("Mac OS X".toLowerCase());
	}
	
	public static boolean isUnix() {
		String OS = System.getProperty("os.name").toLowerCase();
        return (OS.indexOf("nix") >= 0 || OS.indexOf("nux") >= 0 || OS.indexOf("aix") > 0 );
    }
}
