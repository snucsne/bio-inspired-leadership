package me.solhub.simgui;

import java.awt.BorderLayout;
import java.awt.EventQueue;
import java.text.NumberFormat;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.JTabbedPane;
import javax.swing.JSlider;
import javax.swing.JLabel;
import javax.swing.JComboBox;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JCheckBox;
import javax.swing.JFormattedTextField;

import java.beans.PropertyChangeEvent;

import javax.swing.JButton;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;

public class SimulatorLauncherGUI extends JFrame {

	private static final long serialVersionUID = 1L;
	
	
	private JPanel contentPane;
	private JFormattedTextField frmtdtxtfldRunCount;
	private JFormattedTextField frmtdtxtfldSimCount;
	private JFormattedTextField frmtdtxtfldMaxTimeSteps;
	private JSlider sliderAgent;
	private JSpinner spinnerMaxEaten;
	
	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					SimulatorLauncherGUI frame = new SimulatorLauncherGUI();
					frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the frame.
	 */
	public SimulatorLauncherGUI() {
		setTitle("Simulator");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 500, 340);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		contentPane.setLayout(new BorderLayout(0, 0));
		setContentPane(contentPane);
		
		JTabbedPane tabbedPane = new JTabbedPane(JTabbedPane.TOP);
		contentPane.add(tabbedPane, BorderLayout.CENTER);
		
		JPanel panelTab1 = new JPanel();
		tabbedPane.addTab("Simulator", null, panelTab1, null);
		
		JPanel panelAgentCount = new JPanel();
		panelTab1.add(panelAgentCount);
		
		JLabel lblNewLabel = new JLabel("Agent Count");
		panelAgentCount.add(lblNewLabel);
		
		sliderAgent = new JSlider();
		panelAgentCount.add(sliderAgent);
		sliderAgent.setValue(10);
		sliderAgent.setSnapToTicks(true);
		sliderAgent.setPaintTicks(true);
		sliderAgent.setPaintLabels(true);
		sliderAgent.setMinorTickSpacing(10);
		sliderAgent.setMajorTickSpacing(10);
		sliderAgent.setMinimum(10);
		sliderAgent.setMaximum(70);
		
		sliderAgent.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent arg0) {
            	Integer spinnerValue = (Integer)spinnerMaxEaten.getValue();
            	if(spinnerValue > sliderAgent.getValue()){
            		spinnerValue = sliderAgent.getValue();
            	}
            	spinnerMaxEaten.setModel(new SpinnerNumberModel(spinnerValue, new Integer(0), new Integer(sliderAgent.getValue()), new Integer(1)));
            	JFormattedTextField tfMaxEaten = ((JSpinner.DefaultEditor)spinnerMaxEaten.getEditor()).getTextField();
        		tfMaxEaten.setEditable(false);
            }
        });
		
		JPanel panelCommType = new JPanel();
		panelTab1.add(panelCommType);
		
		JLabel lblNewLabel_1 = new JLabel("Communication Type");
		panelCommType.add(lblNewLabel_1);
		
		JComboBox<String> comboBox = new JComboBox<String>();
		panelCommType.add(comboBox);
		comboBox.setModel(new DefaultComboBoxModel<String>(new String[] {"Global", "Topological", "Metric"}));
		
		JPanel panelResultsOutput = new JPanel();
		panelTab1.add(panelResultsOutput);
		
		JLabel lblNewLabel_2 = new JLabel("Results Output");
		panelResultsOutput.add(lblNewLabel_2);
		
		JCheckBox chckbxEskridge = new JCheckBox("Eskridge");
		panelResultsOutput.add(chckbxEskridge);
		
		JCheckBox chckbxConflict = new JCheckBox("Conflict");
		panelResultsOutput.add(chckbxConflict);
		
		JCheckBox chckbxPosition = new JCheckBox("Position");
		panelResultsOutput.add(chckbxPosition);
		
		JCheckBox chckbxPredation = new JCheckBox("Predation");
		panelResultsOutput.add(chckbxPredation);
		
		JPanel panelMisc = new JPanel();
		panelTab1.add(panelMisc);
		
		JLabel lblNewLabel_3 = new JLabel("Misc");
		panelMisc.add(lblNewLabel_3);
		
		JCheckBox chckbxGraphical = new JCheckBox("Graphical?");
		panelMisc.add(chckbxGraphical);
		
		JCheckBox chckbxRandomSeed = new JCheckBox("Random Seed?");
		panelMisc.add(chckbxRandomSeed);
		
		JCheckBox chckbxPredation_1 = new JCheckBox("Predation?");
		panelMisc.add(chckbxPredation_1);
		
		NumberFormat countFormat = NumberFormat.getNumberInstance();
		countFormat.setParseIntegerOnly(true);
		
		JPanel panelCounts = new JPanel();
		panelTab1.add(panelCounts);
		
		JLabel lblNewLabel_4 = new JLabel("Run Count");
		panelCounts.add(lblNewLabel_4);
		
		frmtdtxtfldRunCount = new JFormattedTextField(countFormat);
		panelCounts.add(frmtdtxtfldRunCount);
		frmtdtxtfldRunCount.setColumns(4);
		frmtdtxtfldRunCount.setValue((Number)1);
		frmtdtxtfldRunCount.setText("1");
		
		JLabel lblNewLabel_5 = new JLabel("Sim Count");
		panelCounts.add(lblNewLabel_5);
		
		frmtdtxtfldSimCount = new JFormattedTextField(countFormat);
		frmtdtxtfldSimCount.setColumns(4);
		frmtdtxtfldSimCount.setValue((Number)1);
		panelCounts.add(frmtdtxtfldSimCount);
		
		JLabel lblNewLabel_6 = new JLabel("Max Time Steps");
		panelCounts.add(lblNewLabel_6);
		
		frmtdtxtfldMaxTimeSteps = new JFormattedTextField(countFormat);
		frmtdtxtfldMaxTimeSteps.setColumns(6);
		frmtdtxtfldMaxTimeSteps.setValue((Number)20000);
		panelCounts.add(frmtdtxtfldMaxTimeSteps);
		
		JPanel panelStartButtons = new JPanel();
		panelTab1.add(panelStartButtons);
		
		JButton btnStartSimulation = new JButton("Start Simulation with these Settings");
		panelStartButtons.add(btnStartSimulation);
		
		JButton btnStartSimulationFrom = new JButton("Start Simulation from Properties File");
		panelStartButtons.add(btnStartSimulationFrom);
		
		JPanel panelTab2 = new JPanel();
		tabbedPane.addTab("Parameters", null, panelTab2, null);
		
		JPanel panelDecisionCalculator = new JPanel();
		panelTab2.add(panelDecisionCalculator);
		
		JLabel lblDecisionCalculator = new JLabel("Decision Calculator");
		panelDecisionCalculator.add(lblDecisionCalculator);
		
		JComboBox<String> comboBox_2 = new JComboBox<String>();
		panelDecisionCalculator.add(comboBox_2);
		comboBox_2.setModel(new DefaultComboBoxModel<String>(new String[] {"Default", "Conflict"}));
		
		JPanel panelAgentBuilder = new JPanel();
		panelTab2.add(panelAgentBuilder);
		
		JLabel lblAgentBuilder = new JLabel("Agent Builder");
		panelAgentBuilder.add(lblAgentBuilder);
		
		JComboBox<String> comboBox_3 = new JComboBox<String>();
		panelAgentBuilder.add(comboBox_3);
		comboBox_3.setModel(new DefaultComboBoxModel<String>(new String[] {"Default", "Angular", "AngularConflict"}));
		
		JPanel panelModel = new JPanel();
		panelTab2.add(panelModel);
		
		JLabel lblModel = new JLabel("Model");
		panelModel.add(lblModel);
		
		JComboBox<String> comboBox_1 = new JComboBox<String>();
		panelModel.add(comboBox_1);
		comboBox_1.setModel(new DefaultComboBoxModel<String>(new String[] {"Sueur", "Gautrais"}));
		
		JPanel panelEnvironment = new JPanel();
		panelTab2.add(panelEnvironment);
		
		JLabel lblEnvironment = new JLabel("Environment");
		panelEnvironment.add(lblEnvironment);
		
		JComboBox<String> comboBox_4 = new JComboBox<String>();
		comboBox_4.setModel(new DefaultComboBoxModel<String>(new String[] {"Medium", "Minimum", "Maximum"}));
		panelEnvironment.add(comboBox_4);
		
		JPanel panelDefaultConflict = new JPanel();
		panelTab2.add(panelDefaultConflict);
		
		JLabel lblDefaultConflict = new JLabel("Default Conflict");
		panelDefaultConflict.add(lblDefaultConflict);
		
		JSpinner spinnerConflict = new JSpinner();
		panelDefaultConflict.add(spinnerConflict);
		spinnerConflict.setModel(new SpinnerNumberModel(new Float(0.9f), new Float(0.1f), new Float(0.91f), new Float(0.05)));
		JFormattedTextField tfSpinnerConflict = ((JSpinner.DefaultEditor)spinnerConflict.getEditor()).getTextField();
		tfSpinnerConflict.setEditable(false);
		
		JPanel panelCancelationThreshold = new JPanel();
		panelTab2.add(panelCancelationThreshold);
		
		JLabel lblCancelationThreshold = new JLabel("Cancelation Threshold");
		panelCancelationThreshold.add(lblCancelationThreshold);
		
		JSpinner spinnerCancelationThreshold = new JSpinner();
		panelCancelationThreshold.add(spinnerCancelationThreshold);
		spinnerCancelationThreshold.setModel(new SpinnerNumberModel(new Float(1.0f), new Float(0.0f), new Float(1.01f), new Float(0.05)));
		JFormattedTextField tfCancelationThreshold = ((JSpinner.DefaultEditor)spinnerCancelationThreshold.getEditor()).getTextField();
		tfCancelationThreshold.setEditable(false);
		
		JCheckBox chckbxUsePredationThreshold = new JCheckBox("Use Predation Threshold");
		panelTab2.add(chckbxUsePredationThreshold);
		
		JPanel panelPredationStuff = new JPanel();
		panelTab2.add(panelPredationStuff);
		
		JLabel lblPredationMinimum = new JLabel("Predation Minimum");
		panelPredationStuff.add(lblPredationMinimum);
		
		JSpinner spinnerPredationMinimum = new JSpinner();
		panelPredationStuff.add(spinnerPredationMinimum);
		spinnerPredationMinimum.setModel(new SpinnerNumberModel(new Float(0.0f), new Float(0.0f), new Float(1.01f), new Float(0.001)));
		JFormattedTextField tfPredationMinimum = ((JSpinner.DefaultEditor)spinnerPredationMinimum.getEditor()).getTextField();
		tfPredationMinimum.setEditable(false);
		
		JLabel lblPredationThreshold = new JLabel("Predation Threshold");
		panelPredationStuff.add(lblPredationThreshold);
		
		JSpinner spinnerPredationThreshold = new JSpinner();
		panelPredationStuff.add(spinnerPredationThreshold);
		spinnerPredationThreshold.setModel(new SpinnerNumberModel(new Float(1.0f), new Float(0.0f), new Float(1.01f), new Float(0.05)));
		JFormattedTextField tfPredationThreshold = ((JSpinner.DefaultEditor)spinnerPredationThreshold.getEditor()).getTextField();
		tfPredationThreshold.setEditable(false);
		
		JLabel lblMaxEaten = new JLabel("Max Eaten");
		panelPredationStuff.add(lblMaxEaten);
		
		spinnerMaxEaten = new JSpinner();
		panelPredationStuff.add(spinnerMaxEaten);
		spinnerMaxEaten.setModel(new SpinnerNumberModel(new Integer(1), new Integer(0), new Integer(sliderAgent.getValue()), new Integer(1)));
		JFormattedTextField tfMaxEaten = ((JSpinner.DefaultEditor)spinnerMaxEaten.getEditor()).getTextField();
		tfMaxEaten.setEditable(false);
		
		JPanel panelTab3 = new JPanel();
		tabbedPane.addTab("Environment", null, panelTab3, null);
	}
	
	public void propertyChange(PropertyChangeEvent arg0){
		Object source = arg0.getSource();
		if(source == frmtdtxtfldRunCount){
			frmtdtxtfldRunCount.setValue(((Number)frmtdtxtfldRunCount.getValue()).intValue());
		}
		else if(source == frmtdtxtfldSimCount){
			frmtdtxtfldSimCount.setValue(((Number)frmtdtxtfldSimCount.getValue()).intValue());
		}
		else if(source == frmtdtxtfldMaxTimeSteps){
			frmtdtxtfldMaxTimeSteps.setValue(((Number)frmtdtxtfldMaxTimeSteps.getValue()).intValue());
		}
	}
}
