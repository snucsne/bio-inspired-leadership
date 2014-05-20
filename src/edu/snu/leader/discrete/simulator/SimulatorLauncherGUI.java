package edu.snu.leader.discrete.simulator;

import java.awt.BorderLayout;
import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
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
	private JFormattedTextField frmtdtxtfldNearestNeighborCount;
	private JFormattedTextField frmtdtxtfldMaxLocationRadius;
	private JComboBox<String> comboBoxCommType;
	private JPanel panelMaxLocationRadius;
	private JPanel panelNearestNeighborCount;
	private JFormattedTextField frmtdtxtfldPredationConstant;
	private JComboBox<String> comboBoxModel;
	private JPanel panelSueurValues;
	private JPanel panelGautraisValues;
	private JComboBox<String> comboBoxEnvironment;
	private JPanel panelPercentage;
	private JPanel panelAngle;
	private JPanel panelDistance;
	private JPanel panelNumberOfDestinations;
	private JPanel panelPredationBoxes;
	private JPanel panelPredationStuff;
	private JPanel panelPredationConstant;
	private JCheckBox chckbxPredationEnable;
	
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
		NumberFormat countFormat = NumberFormat.getNumberInstance();
		countFormat.setParseIntegerOnly(true);
		
		NumberFormat doubleFormat = NumberFormat.getNumberInstance();
		doubleFormat.setMinimumIntegerDigits(1);
		doubleFormat.setMaximumFractionDigits(10);
		
		setTitle("Simulator");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 515, 340);
		setResizable(false);
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
		
		comboBoxCommType = new JComboBox<String>();
		panelCommType.add(comboBoxCommType);
		comboBoxCommType.setModel(new DefaultComboBoxModel<String>(new String[] {"Global", "Topological", "Metric"}));
		comboBoxCommType.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent arg0) {
				String item = (String) comboBoxCommType.getSelectedItem();
				if(item.equals("Topological")){
					panelNearestNeighborCount.setVisible(true);
					panelMaxLocationRadius.setVisible(false);
				}
				else if(item.equals("Metric")){
					panelNearestNeighborCount.setVisible(false);
					panelMaxLocationRadius.setVisible(true);
				}
				else if(item.equals("Global")){
					panelNearestNeighborCount.setVisible(false);
					panelMaxLocationRadius.setVisible(false);
				}
			}
		});
		
		JPanel panelResultsOutput = new JPanel();
		panelTab1.add(panelResultsOutput);
		
		JLabel lblResultsOutput = new JLabel("Results Output");
		panelResultsOutput.add(lblResultsOutput);
		
		JCheckBox chckbxEskridge = new JCheckBox("Eskridge");
		panelResultsOutput.add(chckbxEskridge);
		
		JCheckBox chckbxConflict = new JCheckBox("Conflict");
		panelResultsOutput.add(chckbxConflict);
		
		JCheckBox chckbxPosition = new JCheckBox("Position");
		panelResultsOutput.add(chckbxPosition);
		
		JCheckBox chckbxPredationResults = new JCheckBox("Predation");
		panelResultsOutput.add(chckbxPredationResults);
		
		JPanel panelMisc = new JPanel();
		panelTab1.add(panelMisc);
		
		JLabel lblNewLabel_3 = new JLabel("Misc");
		panelMisc.add(lblNewLabel_3);
		
		JCheckBox chckbxGraphical = new JCheckBox("Graphical?");
		panelMisc.add(chckbxGraphical);
		
		JCheckBox chckbxRandomSeed = new JCheckBox("Random Seed?");
		panelMisc.add(chckbxRandomSeed);
		
		chckbxPredationEnable = new JCheckBox("Predation?");
		chckbxPredationEnable.setSelected(true);
		chckbxPredationEnable.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent arg0) {
				if(chckbxPredationEnable.isSelected()){
					panelPredationStuff.setVisible(true);
					panelPredationBoxes.setVisible(true);
					panelPredationConstant.setVisible(true);
				}
				else{
					panelPredationStuff.setVisible(false);
					panelPredationBoxes.setVisible(false);
					panelPredationConstant.setVisible(false);
				}
			}
		});
		panelMisc.add(chckbxPredationEnable);
		
		JPanel panelCounts = new JPanel();
		panelTab1.add(panelCounts);
		
		JLabel lblNewLabel_4 = new JLabel("Run Count");
		panelCounts.add(lblNewLabel_4);
		
		frmtdtxtfldRunCount = new JFormattedTextField(countFormat);
		frmtdtxtfldRunCount.setToolTipText("The number of runs. Each run has a different random seed.");
		panelCounts.add(frmtdtxtfldRunCount);
		frmtdtxtfldRunCount.setColumns(4);
		frmtdtxtfldRunCount.setValue((Number)1);
		
		JLabel lblNewLabel_5 = new JLabel("Sim Count");
		panelCounts.add(lblNewLabel_5);
		
		frmtdtxtfldSimCount = new JFormattedTextField(countFormat);
		frmtdtxtfldSimCount.setToolTipText("The number of simulations per run. Each simulation uses the same random seed.");
		frmtdtxtfldSimCount.setColumns(4);
		frmtdtxtfldSimCount.setValue((Number)1);
		panelCounts.add(frmtdtxtfldSimCount);
		
		JLabel lblNewLabel_6 = new JLabel("Max Time Steps");
		panelCounts.add(lblNewLabel_6);
		
		frmtdtxtfldMaxTimeSteps = new JFormattedTextField(countFormat);
		frmtdtxtfldMaxTimeSteps.setToolTipText("The max number of time steps per simulation.");
		frmtdtxtfldMaxTimeSteps.setColumns(6);
		frmtdtxtfldMaxTimeSteps.setValue((Number)20000);
		panelCounts.add(frmtdtxtfldMaxTimeSteps);
		
		JPanel panelStartButtons = new JPanel();
		
		JButton btnStartSimulation = new JButton("Create Simulator Instance");
		btnStartSimulation.setToolTipText("Creates a new simulator instance from the settings provided.");
		panelStartButtons.add(btnStartSimulation);
		
		JButton btnStartSimulationFrom = new JButton("Run Simulation from Properties File");
		btnStartSimulationFrom.setToolTipText("Runs the simulator with the values provided in the properties file.");
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
		
		comboBoxModel = new JComboBox<String>();
		panelModel.add(comboBoxModel);
		comboBoxModel.setModel(new DefaultComboBoxModel<String>(new String[] {"Sueur", "Gautrais"}));
		comboBoxModel.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent arg0) {
				String item = (String)comboBoxModel.getSelectedItem();
				if(item.equals("Sueur")){
					panelSueurValues.setVisible(true);
					panelGautraisValues.setVisible(false);
				}
				else if(item.equals("Gautrais")){
					panelSueurValues.setVisible(false);
					panelGautraisValues.setVisible(true);
				}
			}
		});
		
		JPanel panelEnvironment = new JPanel();
		panelTab2.add(panelEnvironment);
		
		JLabel lblEnvironment = new JLabel("Environment");
		panelEnvironment.add(lblEnvironment);
		
		comboBoxEnvironment = new JComboBox<String>();
		comboBoxEnvironment.setModel(new DefaultComboBoxModel<String>(new String[] {"Medium", "Minimum", "Maximum"}));
		comboBoxEnvironment.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent arg0) {
				String item = (String)comboBoxEnvironment.getSelectedItem();
				if(item.equals("Medium")){
					panelAngle.setVisible(true);
					panelDistance.setVisible(true);
					panelPercentage.setVisible(true);
					panelNumberOfDestinations.setVisible(false);
				}
				else if(item.equals("Minimum")){
					panelAngle.setVisible(false);
					panelDistance.setVisible(false);
					panelPercentage.setVisible(true);
					panelNumberOfDestinations.setVisible(false);
				}
				else if(item.equals("Maximum")){
					panelAngle.setVisible(false);
					panelDistance.setVisible(false);
					panelPercentage.setVisible(true);
					panelNumberOfDestinations.setVisible(false);
				}
			}
		});
		panelEnvironment.add(comboBoxEnvironment);
		
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
		
		panelNearestNeighborCount = new JPanel();
		panelNearestNeighborCount.setVisible(false);
		panelTab2.add(panelNearestNeighborCount);
		
		JLabel lblNearestNeighborCount = new JLabel("Nearest Neighbor Count");
		panelNearestNeighborCount.add(lblNearestNeighborCount);
		
		frmtdtxtfldNearestNeighborCount = new JFormattedTextField(countFormat);
		panelNearestNeighborCount.add(frmtdtxtfldNearestNeighborCount);
		frmtdtxtfldNearestNeighborCount.setColumns(3);
		frmtdtxtfldNearestNeighborCount.setValue((Number)10);
		
		panelMaxLocationRadius = new JPanel();
		panelMaxLocationRadius.setVisible(false);
		panelTab2.add(panelMaxLocationRadius);
		
		JLabel lblMaxLocationRadius = new JLabel("Max Location Radius");
		panelMaxLocationRadius.add(lblMaxLocationRadius);
		
		frmtdtxtfldMaxLocationRadius = new JFormattedTextField(doubleFormat);
		panelMaxLocationRadius.add(frmtdtxtfldMaxLocationRadius);
		frmtdtxtfldMaxLocationRadius.setColumns(5);
		frmtdtxtfldMaxLocationRadius.setValue((Number)10.0);
		
		panelPredationBoxes = new JPanel();
		panelTab2.add(panelPredationBoxes);
		
		JCheckBox chckbxUsePredationThreshold = new JCheckBox("Use Predation Threshold");
		panelPredationBoxes.add(chckbxUsePredationThreshold);
		
		JCheckBox chckbxPopulationIndependent = new JCheckBox("Population Independent");
		panelPredationBoxes.add(chckbxPopulationIndependent);
		chckbxPopulationIndependent.setToolTipText("Select this to allow predation to be independent of population size. Max predation for 10 agents will be the same as for 50 agents. ");
		
		panelPredationStuff = new JPanel();
		panelTab2.add(panelPredationStuff);
		
		JLabel lblPredationMinimum = new JLabel("Predation Minimum");
		panelPredationStuff.add(lblPredationMinimum);
		
		JFormattedTextField frmtdtxtfldPredationMinimum = new JFormattedTextField(doubleFormat);
		frmtdtxtfldPredationMinimum.setColumns(4);
		frmtdtxtfldPredationMinimum.setValue((Number)0.0);
		panelPredationStuff.add(frmtdtxtfldPredationMinimum);
		
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
		spinnerMaxEaten.setToolTipText("The max number eaten per time step.");
		panelPredationStuff.add(spinnerMaxEaten);
		spinnerMaxEaten.setModel(new SpinnerNumberModel(new Integer(1), new Integer(0), new Integer(sliderAgent.getValue()), new Integer(1)));
		JFormattedTextField tfMaxEaten = ((JSpinner.DefaultEditor)spinnerMaxEaten.getEditor()).getTextField();
		tfMaxEaten.setEditable(false);
		
		panelPredationConstant = new JPanel();
		panelTab2.add(panelPredationConstant);
		
		JLabel lblPredationConstant = new JLabel("Predation Constant");
		panelPredationConstant.add(lblPredationConstant);
		
		frmtdtxtfldPredationConstant = new JFormattedTextField(doubleFormat);
		panelPredationConstant.add(frmtdtxtfldPredationConstant);
		frmtdtxtfldPredationConstant.setToolTipText("Value should be positive. Recommended values are near 0.001");
		frmtdtxtfldPredationConstant.setColumns(4);
		frmtdtxtfldPredationConstant.setValue((Number)0.001);
		
		JPanel panelTab3 = new JPanel();
		tabbedPane.addTab("Environment", null, panelTab3, null);
		
		panelSueurValues = new JPanel();
		panelTab3.add(panelSueurValues);
		
		JLabel lblAlpha = new JLabel("alpha");
		panelSueurValues.add(lblAlpha);
		
		JFormattedTextField frmtdtxtfldAlpha = new JFormattedTextField(doubleFormat);
		frmtdtxtfldAlpha.setColumns(6);
		frmtdtxtfldAlpha.setValue((Number)0.006161429);
		panelSueurValues.add(frmtdtxtfldAlpha);
		
		JLabel lblAlphaC = new JLabel("alpha c");
		panelSueurValues.add(lblAlphaC);
		
		JFormattedTextField frmtdtxtfldAlphaC = new JFormattedTextField(doubleFormat);
		frmtdtxtfldAlphaC.setColumns(3);
		frmtdtxtfldAlphaC.setValue((Number)0.009);
		panelSueurValues.add(frmtdtxtfldAlphaC);
		
		JLabel lblBeta = new JLabel("beta");
		panelSueurValues.add(lblBeta);
		
		JFormattedTextField frmtdtxtfldBeta = new JFormattedTextField(doubleFormat);
		frmtdtxtfldBeta.setColumns(6);
		frmtdtxtfldBeta.setValue((Number)0.013422819);
		panelSueurValues.add(frmtdtxtfldBeta);
		
		JLabel lblBetaC = new JLabel("beta c");
		panelSueurValues.add(lblBetaC);
		
		JFormattedTextField frmtdtxtfldBetaC = new JFormattedTextField(doubleFormat);
		frmtdtxtfldBetaC.setColumns(4);
		frmtdtxtfldBetaC.setValue((Number)(-0.009));
		panelSueurValues.add(frmtdtxtfldBetaC);
		
		JLabel lblS = new JLabel("S");
		panelSueurValues.add(lblS);
		
		JFormattedTextField frmtdtxtfldS = new JFormattedTextField(countFormat);
		frmtdtxtfldS.setColumns(2);
		frmtdtxtfldS.setValue((Number)2);
		panelSueurValues.add(frmtdtxtfldS);
		
		JLabel lblQ = new JLabel("q");
		panelSueurValues.add(lblQ);
		
		JFormattedTextField frmtdtxtfldQ = new JFormattedTextField(doubleFormat);
		frmtdtxtfldQ.setColumns(2);
		frmtdtxtfldQ.setValue((Number)2.3);
		panelSueurValues.add(frmtdtxtfldQ);
		
		panelGautraisValues = new JPanel();
		panelGautraisValues.setVisible(false);
		panelTab3.add(panelGautraisValues);
		
		JLabel lblTauO = new JLabel("tau o");
		panelGautraisValues.add(lblTauO);
		
		JFormattedTextField frmtdtxtfldTaoO = new JFormattedTextField(doubleFormat);
		frmtdtxtfldTaoO.setColumns(4);
		frmtdtxtfldTaoO.setValue((Number)1290);
		panelGautraisValues.add(frmtdtxtfldTaoO);
		
		JLabel lblGammaC = new JLabel("gamma c");
		panelGautraisValues.add(lblGammaC);
		
		JFormattedTextField frmtdtxtfldGammaC = new JFormattedTextField(doubleFormat);
		frmtdtxtfldGammaC.setColumns(2);
		frmtdtxtfldGammaC.setValue((Number)2.0);
		panelGautraisValues.add(frmtdtxtfldGammaC);
		
		JLabel lblEpsilonC = new JLabel("epsilon c");
		panelGautraisValues.add(lblEpsilonC);
		
		JFormattedTextField frmtdtxtfldEpsilonC = new JFormattedTextField(doubleFormat);
		frmtdtxtfldEpsilonC.setColumns(3);
		frmtdtxtfldEpsilonC.setValue((Number)2.3);
		panelGautraisValues.add(frmtdtxtfldEpsilonC);
		
		JLabel lblAlphaF = new JLabel("alpha f");
		panelGautraisValues.add(lblAlphaF);
		
		JFormattedTextField frmtdtxtfldAlphaF = new JFormattedTextField(doubleFormat);
		frmtdtxtfldAlphaF.setColumns(4);
		frmtdtxtfldAlphaF.setValue((Number)162.3);
		panelGautraisValues.add(frmtdtxtfldAlphaF);
		
		JLabel lblBetaF = new JLabel("beta f");
		panelGautraisValues.add(lblBetaF);
		
		JFormattedTextField frmtdtxtfldBetaF = new JFormattedTextField(doubleFormat);
		frmtdtxtfldBetaF.setColumns(3);
		frmtdtxtfldBetaF.setValue((Number)75.4);
		panelGautraisValues.add(frmtdtxtfldBetaF);
		
		JPanel panelEnvironmentVariables = new JPanel();
		panelTab3.add(panelEnvironmentVariables);
		
		panelAngle = new JPanel();
		panelEnvironmentVariables.add(panelAngle);
		
		JLabel lblAngle = new JLabel("Angle");
		panelAngle.add(lblAngle);
		
		JFormattedTextField frmtdtxtfldAngle = new JFormattedTextField(doubleFormat);
		frmtdtxtfldAngle.setToolTipText("Angle between destinations");
		panelAngle.add(frmtdtxtfldAngle);
		frmtdtxtfldAngle.setColumns(3);
		frmtdtxtfldAngle.setValue((Number)72.00);
		
		panelNumberOfDestinations = new JPanel();
		panelNumberOfDestinations.setVisible(false);
		panelEnvironmentVariables.add(panelNumberOfDestinations);
		
		JLabel lblNumberOfDestinations = new JLabel("Number of Destinations");
		panelNumberOfDestinations.add(lblNumberOfDestinations);
		
		JFormattedTextField frmtdtxtfldNumberOfDestinations = new JFormattedTextField(countFormat);
		panelNumberOfDestinations.add(frmtdtxtfldNumberOfDestinations);
		frmtdtxtfldNumberOfDestinations.setColumns(3);
		frmtdtxtfldNumberOfDestinations.setValue((Number)2);
		
		panelDistance = new JPanel();
		panelEnvironmentVariables.add(panelDistance);
		
		JLabel lblDistance = new JLabel("Distance");
		panelDistance.add(lblDistance);
		
		JFormattedTextField frmtdtxtfldDistance = new JFormattedTextField(doubleFormat);
		frmtdtxtfldDistance.setToolTipText("Distance the destination is from origin (0,0)");
		panelDistance.add(frmtdtxtfldDistance);
		frmtdtxtfldDistance.setColumns(3);
		frmtdtxtfldDistance.setValue((Number)150.0);
		
		panelPercentage = new JPanel();
		panelEnvironmentVariables.add(panelPercentage);
		
		JLabel lblPercentage = new JLabel("Percentage");
		panelPercentage.add(lblPercentage);
		
		JFormattedTextField frmtdtxtfldPercentage = new JFormattedTextField(doubleFormat);
		frmtdtxtfldPercentage.setToolTipText("The percentage moving to one of the two destinations ( The other gets 1 - percentage).");
		panelPercentage.add(frmtdtxtfldPercentage);
		frmtdtxtfldPercentage.setColumns(3);
		frmtdtxtfldPercentage.setValue((Number)0.500);
		
		panelTab3.add(panelStartButtons);
	}
	
//	public void propertyChange(PropertyChangeEvent arg0){
//		Object source = arg0.getSource();
//		if(source == frmtdtxtfldRunCount){
//			frmtdtxtfldRunCount.setValue(((Number)frmtdtxtfldRunCount.getValue()).intValue());
//		}
//		else if(source == frmtdtxtfldSimCount){
//			frmtdtxtfldSimCount.setValue(((Number)frmtdtxtfldSimCount.getValue()).intValue());
//		}
//		else if(source == frmtdtxtfldMaxTimeSteps){
//			frmtdtxtfldMaxTimeSteps.setValue(((Number)frmtdtxtfldMaxTimeSteps.getValue()).intValue());
//		}
//		else if(source == frmtdtxtfldNearestNeighborCount){
//			frmtdtxtfldNearestNeighborCount.setValue(((Number)frmtdtxtfldNearestNeighborCount.getValue()).intValue());
//		}
//		else if(source == frmtdtxtfldMaxLocationRadius){
//			frmtdtxtfldMaxLocationRadius.setValue(((Number)frmtdtxtfldMaxLocationRadius.getValue()).doubleValue());
//		}
//		else if(source == frmtdtxtfldPredationConstant){
//			frmtdtxtfldPredationConstant.setValue(((Number)frmtdtxtfldPredationConstant.getValue()).doubleValue());
//		}
//	}
}
