/*
 *  The Bio-inspired Leadership Toolkit is a set of tools used to
 *  simulate the emergence of leaders in multi-agent systems.
 *  Copyright (C) 2014 Southern Nazarene University
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package edu.snu.leader.discrete.simulator;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

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
import javax.swing.JTextPane;
import javax.swing.SpinnerNumberModel;
import javax.swing.BoxLayout;

import java.awt.FlowLayout;

import javax.swing.SwingConstants;

import me.solhub.simple.engine.DebugLocationsStructure;

import org.apache.commons.lang.Validate;

import edu.snu.leader.discrete.utils.DestinationBuilder;

import java.awt.Component;

public class SimulatorLauncherGUI extends JFrame {

    private static final long serialVersionUID = 1L;
    
    private Properties _simulatorProperties = null;
    private JPanel contentPane;
    private JTabbedPane tabbedPane;
    private JFrame jframeErrorMessages = null;
    
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
    private JPanel panelNonMoversSurvive;
    private JCheckBox chckbxPredationEnable;
    private JFormattedTextField frmtdtxtfldPredationMinimum;
    private JFormattedTextField frmtdtxtfldPercentage;
    private JFormattedTextField frmtdtxtfldDistance;
    private JCheckBox chckbxGraphical;
    private JCheckBox chckbxRandomSeed;
    private JFormattedTextField frmtdtxtfldDestinationRadius;
    private JComboBox<String> comboBoxAgentBuilder;
    private JPanel panelInformedCount;
    
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
        
        tabbedPane = new JTabbedPane(JTabbedPane.TOP);
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
        
        JPanel panelDestinationRadius = new JPanel();
        panelTab1.add(panelDestinationRadius);
        
        JLabel lblDestinationRadius = new JLabel("Destination Radius");
        panelDestinationRadius.add(lblDestinationRadius);
        
        frmtdtxtfldDestinationRadius = new JFormattedTextField(doubleFormat);
        frmtdtxtfldDestinationRadius.setColumns(4);
        frmtdtxtfldDestinationRadius.setValue((Number)10);
        panelDestinationRadius.add(frmtdtxtfldDestinationRadius);
        
        JPanel panelResultsOutput = new JPanel();
        panelTab1.add(panelResultsOutput);
        
        JLabel lblResultsOutput = new JLabel("Results Output");
        panelResultsOutput.add(lblResultsOutput);
        
        final JCheckBox chckbxEskridge = new JCheckBox("Eskridge");
        panelResultsOutput.add(chckbxEskridge);
        
        final JCheckBox chckbxConflict = new JCheckBox("Conflict");
        panelResultsOutput.add(chckbxConflict);
        
        final JCheckBox chckbxPosition = new JCheckBox("Position");
        panelResultsOutput.add(chckbxPosition);
        
        final JCheckBox chckbxPredationResults = new JCheckBox("Predation");
        panelResultsOutput.add(chckbxPredationResults);
        
        JPanel panelMisc = new JPanel();
        panelTab1.add(panelMisc);
        
        JLabel lblNewLabel_3 = new JLabel("Misc");
        panelMisc.add(lblNewLabel_3);
        
        chckbxGraphical = new JCheckBox("Graphical?");
        chckbxGraphical.addActionListener( new ActionListener(){

            @Override
            public void actionPerformed( ActionEvent arg0 )
            {
                if(chckbxGraphical.isSelected()){
                    frmtdtxtfldRunCount.setValue( (Number)1 );
                    frmtdtxtfldRunCount.setEnabled( false );
                    chckbxEskridge.setSelected( false );
                    chckbxEskridge.setEnabled( false );
                    String agentBuilder = (String) comboBoxAgentBuilder.getSelectedItem();
                    if(agentBuilder.equals( "Default" )){
                        comboBoxAgentBuilder.setSelectedIndex( 1 );
                    }
                }
                else{
                    chckbxEskridge.setEnabled( true );
                    frmtdtxtfldRunCount.setEnabled( true );
                }
            }
            
        });
        panelMisc.add(chckbxGraphical);
        
        chckbxRandomSeed = new JCheckBox("Random Seed?");
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
                    panelNonMoversSurvive.setVisible( true );
                }
                else{
                    panelPredationStuff.setVisible(false);
                    panelPredationBoxes.setVisible(false);
                    panelPredationConstant.setVisible(false);
                    panelNonMoversSurvive.setVisible( false );
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
        
        
        ////////Panel tab 2
        
        
        JPanel panelTab2 = new JPanel();
        tabbedPane.addTab("Parameters", null, panelTab2, null);
        
        JPanel panelDecisionCalculator = new JPanel();
        panelTab2.add(panelDecisionCalculator);
        
        JLabel lblDecisionCalculator = new JLabel("Decision Calculator");
        panelDecisionCalculator.add(lblDecisionCalculator);
        
        final JComboBox<String> comboBoxDecisionCalculator = new JComboBox<String>();
        panelDecisionCalculator.add(comboBoxDecisionCalculator);
        comboBoxDecisionCalculator.setModel(new DefaultComboBoxModel<String>(new String[] {"Default", "Conflict", "Conflict Uninformed"}));
        
        JPanel panelAgentBuilder = new JPanel();
        panelTab2.add(panelAgentBuilder);
        
        JLabel lblAgentBuilder = new JLabel("Agent Builder");
        panelAgentBuilder.add(lblAgentBuilder);
        
        comboBoxAgentBuilder = new JComboBox<String>();
        panelAgentBuilder.add(comboBoxAgentBuilder);
        comboBoxAgentBuilder.setModel(new DefaultComboBoxModel<String>(new String[] {"Default", "Simple Angular", "Personality Simple Angular", "Simple Angular Uninformed"}));
        comboBoxAgentBuilder.setSelectedIndex( 1 );
        comboBoxAgentBuilder.addActionListener( new ActionListener(){
            @Override
            public void actionPerformed( ActionEvent arg0 )
            {
                if(chckbxGraphical.isSelected()){
                    String agentBuilder = (String) comboBoxAgentBuilder.getSelectedItem();
                    if(agentBuilder.equals( "Default" )){
                        comboBoxAgentBuilder.setSelectedIndex( 1 );
                    }
                }
            }
        });
        
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
        comboBoxEnvironment.setModel(new DefaultComboBoxModel<String>(new String[] {"Minimum", "Medium", "Maximum", "Uninformed"}));
        comboBoxEnvironment.setSelectedIndex( 1 );
        comboBoxEnvironment.addActionListener(new ActionListener(){
            @Override
            public void actionPerformed(ActionEvent arg0) {
                String item = (String)comboBoxEnvironment.getSelectedItem();
                if(!item.equals( "Uninformed" )){
                    comboBoxDecisionCalculator.setEnabled( true );
                    comboBoxAgentBuilder.setEnabled( true );
                }
                
                if(item.equals("Medium")){
                    panelAngle.setVisible(true);
                    panelDistance.setVisible(true);
                    panelPercentage.setVisible(true);
                    panelNumberOfDestinations.setVisible(false);
                    panelInformedCount.setVisible( false );
                }
                else if(item.equals("Minimum")){
                    panelAngle.setVisible(false);
                    panelDistance.setVisible(false);
                    panelPercentage.setVisible(true);
                    panelNumberOfDestinations.setVisible(false);
                    panelInformedCount.setVisible( false );
                }
                else if(item.equals("Maximum")){
                    panelAngle.setVisible(false);
                    panelDistance.setVisible(false);
                    panelPercentage.setVisible(true);
                    panelNumberOfDestinations.setVisible(false);
                    panelInformedCount.setVisible( false );
                }
                else if(item.equals( "Uninformed" )){
                    panelAngle.setVisible(true);
                    panelDistance.setVisible(true);
                    panelPercentage.setVisible(true);
                    panelNumberOfDestinations.setVisible(false);
                    panelInformedCount.setVisible( true );
                    comboBoxDecisionCalculator.setSelectedIndex( 2 );
                    comboBoxDecisionCalculator.setEnabled( false );
                    comboBoxAgentBuilder.setSelectedIndex( 3 );
                    comboBoxAgentBuilder.setEnabled( false );
                }
            }
        });
        panelEnvironment.add(comboBoxEnvironment);
        
        JPanel panelDefaultConflict = new JPanel();
        panelTab2.add(panelDefaultConflict);
        
        JLabel lblDefaultConflict = new JLabel("Default Conflict");
        panelDefaultConflict.add(lblDefaultConflict);
        
        final JSpinner spinnerDefaultConflict = new JSpinner();
        panelDefaultConflict.add(spinnerDefaultConflict);
        spinnerDefaultConflict.setModel(new SpinnerNumberModel(new Float(0.9f), new Float(0.1f), new Float(0.91f), new Float(0.05)));
        JFormattedTextField tfSpinnerConflict = ((JSpinner.DefaultEditor)spinnerDefaultConflict.getEditor()).getTextField();
        tfSpinnerConflict.setEditable(false);
        
        JPanel panelCancelationThreshold = new JPanel();
        panelTab2.add(panelCancelationThreshold);
        
        JLabel lblCancelationThreshold = new JLabel("Cancelation Threshold");
        panelCancelationThreshold.add(lblCancelationThreshold);
        
        final JSpinner spinnerCancelationThreshold = new JSpinner();
        panelCancelationThreshold.add(spinnerCancelationThreshold);
        spinnerCancelationThreshold.setModel(new SpinnerNumberModel(new Float(1.0f), new Float(0.0f), new Float(1.01f), new Float(0.05)));
        JFormattedTextField tfCancelationThreshold = ((JSpinner.DefaultEditor)spinnerCancelationThreshold.getEditor()).getTextField();
        tfCancelationThreshold.setEditable(false);
        
        JPanel panelStopAnywhere = new JPanel();
        panelTab2.add( panelStopAnywhere );
        
        final JCheckBox chckbxStopAnywhere = new JCheckBox( "Stop Anywhere?" );
        panelStopAnywhere.add( chckbxStopAnywhere );
        
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
        
        final JCheckBox chckbxUsePredationThreshold = new JCheckBox("Use Predation Threshold");
        panelPredationBoxes.add(chckbxUsePredationThreshold);
        
        final JCheckBox chckbxPopulationIndependent = new JCheckBox("Population Independent");
        chckbxPopulationIndependent.setSelected( true );
        panelPredationBoxes.add(chckbxPopulationIndependent);
        chckbxPopulationIndependent.setToolTipText("Select this to allow predation to be independent of population size. Max predation for 10 agents will be the same as for 50 agents. ");
        
        panelPredationStuff = new JPanel();
        panelTab2.add(panelPredationStuff);
        
        JLabel lblPredationMinimum = new JLabel("Predation Minimum");
        panelPredationStuff.add(lblPredationMinimum);
        
        frmtdtxtfldPredationMinimum = new JFormattedTextField(doubleFormat);
        frmtdtxtfldPredationMinimum.setColumns(4);
        frmtdtxtfldPredationMinimum.setValue((Number)0.0);
        panelPredationStuff.add(frmtdtxtfldPredationMinimum);
        
        JLabel lblPredationThreshold = new JLabel("Predation Threshold");
        panelPredationStuff.add(lblPredationThreshold);
        
        final JSpinner spinnerPredationThreshold = new JSpinner();
        panelPredationStuff.add(spinnerPredationThreshold);
        spinnerPredationThreshold.setModel(new SpinnerNumberModel(new Float(1.0f), new Float(0.0f), new Float(1.01f), new Float(0.05)));
        JFormattedTextField tfPredationThreshold = ((JSpinner.DefaultEditor)spinnerPredationThreshold.getEditor()).getTextField();
        tfPredationThreshold.setEditable(false);
        
        JLabel lblMaxEaten = new JLabel("Max Eaten");
        panelPredationStuff.add(lblMaxEaten);
        
        spinnerMaxEaten = new JSpinner();
        spinnerMaxEaten.setToolTipText("The max number eaten per time step.");
        panelPredationStuff.add(spinnerMaxEaten);
        spinnerMaxEaten.setModel(new SpinnerNumberModel(new Integer(10), new Integer(0), new Integer(sliderAgent.getValue()), new Integer(1)));
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
        
        panelNonMoversSurvive = new JPanel();
        panelTab2.add( panelNonMoversSurvive );
        
        final JCheckBox chckbxNonMoversSurvive = new JCheckBox("Non-movers Survive?");
        chckbxNonMoversSurvive.setSelected( false );
        panelNonMoversSurvive.add( chckbxNonMoversSurvive );
        
        
        ////////Tab 3
        
        JPanel panelTab3 = new JPanel();
        tabbedPane.addTab("Environment", null, panelTab3, null);
        panelTab3.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));
        
        panelSueurValues = new JPanel();
        panelTab3.add(panelSueurValues);
        panelSueurValues.setLayout(new BoxLayout(panelSueurValues, BoxLayout.Y_AXIS));
        
        JLabel lblSueurValues = new JLabel("Sueur Values");
        lblSueurValues.setHorizontalAlignment(SwingConstants.TRAILING);
        lblSueurValues.setAlignmentX(Component.CENTER_ALIGNMENT);
        panelSueurValues.add(lblSueurValues);
        
        JPanel panelAlpha = new JPanel();
        FlowLayout flowLayout = (FlowLayout) panelAlpha.getLayout();
        flowLayout.setAlignment(FlowLayout.RIGHT);
        panelSueurValues.add(panelAlpha);
        
        JLabel lblAlpha = new JLabel("alpha");
        lblAlpha.setHorizontalAlignment(SwingConstants.CENTER);
        panelAlpha.add(lblAlpha);
        
        final JFormattedTextField frmtdtxtfldAlpha = new JFormattedTextField(doubleFormat);
        frmtdtxtfldAlpha.setHorizontalAlignment(SwingConstants.TRAILING);
        lblAlpha.setLabelFor(frmtdtxtfldAlpha);
        panelAlpha.add(frmtdtxtfldAlpha);
        frmtdtxtfldAlpha.setColumns(6);
        frmtdtxtfldAlpha.setValue((Number)0.006161429);
        
        JPanel panelAlphaC = new JPanel();
        FlowLayout flowLayout_2 = (FlowLayout) panelAlphaC.getLayout();
        flowLayout_2.setAlignment(FlowLayout.RIGHT);
        panelSueurValues.add(panelAlphaC);
        
        JLabel lblAlphaC = new JLabel("alpha c");
        panelAlphaC.add(lblAlphaC);
        
        final JFormattedTextField frmtdtxtfldAlphaC = new JFormattedTextField(doubleFormat);
        frmtdtxtfldAlphaC.setHorizontalAlignment(SwingConstants.TRAILING);
        lblAlphaC.setLabelFor(frmtdtxtfldAlphaC);
        panelAlphaC.add(frmtdtxtfldAlphaC);
        frmtdtxtfldAlphaC.setColumns(6);
        frmtdtxtfldAlphaC.setValue((Number)0.009);
        
        JPanel panelBeta = new JPanel();
        FlowLayout flowLayout_1 = (FlowLayout) panelBeta.getLayout();
        flowLayout_1.setAlignment(FlowLayout.RIGHT);
        panelSueurValues.add(panelBeta);
        
        JLabel lblBeta = new JLabel("beta");
        panelBeta.add(lblBeta);
        
        final JFormattedTextField frmtdtxtfldBeta = new JFormattedTextField(doubleFormat);
        frmtdtxtfldBeta.setHorizontalAlignment(SwingConstants.TRAILING);
        panelBeta.add(frmtdtxtfldBeta);
        frmtdtxtfldBeta.setColumns(6);
        frmtdtxtfldBeta.setValue((Number)0.013422819);
        
        JPanel panelBetaC = new JPanel();
        FlowLayout flowLayout_14 = (FlowLayout) panelBetaC.getLayout();
        flowLayout_14.setAlignment(FlowLayout.RIGHT);
        panelSueurValues.add(panelBetaC);
        
        JLabel lblBetaC = new JLabel("beta c");
        panelBetaC.add(lblBetaC);
        
        final JFormattedTextField frmtdtxtfldBetaC = new JFormattedTextField(doubleFormat);
        frmtdtxtfldBetaC.setHorizontalAlignment(SwingConstants.TRAILING);
        panelBetaC.add(frmtdtxtfldBetaC);
        frmtdtxtfldBetaC.setColumns(6);
        frmtdtxtfldBetaC.setValue((Number)(-0.009));
        
        JPanel panelS = new JPanel();
        FlowLayout flowLayout_3 = (FlowLayout) panelS.getLayout();
        flowLayout_3.setAlignment(FlowLayout.RIGHT);
        panelSueurValues.add(panelS);
        
        JLabel lblS = new JLabel("S");
        panelS.add(lblS);
        
        final JFormattedTextField frmtdtxtfldS = new JFormattedTextField(countFormat);
        frmtdtxtfldS.setHorizontalAlignment(SwingConstants.TRAILING);
        panelS.add(frmtdtxtfldS);
        frmtdtxtfldS.setColumns(6);
        frmtdtxtfldS.setValue((Number)2);
        
        JPanel panelQ = new JPanel();
        FlowLayout flowLayout_4 = (FlowLayout) panelQ.getLayout();
        flowLayout_4.setAlignment(FlowLayout.RIGHT);
        panelSueurValues.add(panelQ);
        
        JLabel lblQ = new JLabel("q");
        panelQ.add(lblQ);
        
        final JFormattedTextField frmtdtxtfldQ = new JFormattedTextField(doubleFormat);
        frmtdtxtfldQ.setHorizontalAlignment(SwingConstants.TRAILING);
        panelQ.add(frmtdtxtfldQ);
        frmtdtxtfldQ.setColumns(6);
        frmtdtxtfldQ.setValue((Number)2.3);
        
        panelGautraisValues = new JPanel();
        panelGautraisValues.setVisible(false);
        panelTab3.add(panelGautraisValues);
        panelGautraisValues.setLayout(new BoxLayout(panelGautraisValues, BoxLayout.Y_AXIS));
        
        JLabel label = new JLabel("Gautrais Values");
        label.setAlignmentX(Component.CENTER_ALIGNMENT);
        panelGautraisValues.add(label);
        
        JPanel panelTauO = new JPanel();
        FlowLayout flowLayout_5 = (FlowLayout) panelTauO.getLayout();
        flowLayout_5.setAlignment(FlowLayout.RIGHT);
        panelGautraisValues.add(panelTauO);
        
        JLabel lblTauO = new JLabel("tau o");
        panelTauO.add(lblTauO);
        
        final JFormattedTextField frmtdtxtfldTaoO = new JFormattedTextField(doubleFormat);
        frmtdtxtfldTaoO.setHorizontalAlignment(SwingConstants.TRAILING);
        panelTauO.add(frmtdtxtfldTaoO);
        frmtdtxtfldTaoO.setColumns(4);
        frmtdtxtfldTaoO.setValue((Number)1290);
        
        JPanel panelGammaC = new JPanel();
        FlowLayout flowLayout_6 = (FlowLayout) panelGammaC.getLayout();
        flowLayout_6.setAlignment(FlowLayout.RIGHT);
        panelGautraisValues.add(panelGammaC);
        
        JLabel lblGammaC = new JLabel("gamma c");
        panelGammaC.add(lblGammaC);
        
        final JFormattedTextField frmtdtxtfldGammaC = new JFormattedTextField(doubleFormat);
        frmtdtxtfldGammaC.setHorizontalAlignment(SwingConstants.TRAILING);
        panelGammaC.add(frmtdtxtfldGammaC);
        frmtdtxtfldGammaC.setColumns(4);
        frmtdtxtfldGammaC.setValue((Number)2.0);
        
        JPanel panelEpsilonC = new JPanel();
        FlowLayout flowLayout_7 = (FlowLayout) panelEpsilonC.getLayout();
        flowLayout_7.setAlignment(FlowLayout.RIGHT);
        panelGautraisValues.add(panelEpsilonC);
        
        JLabel lblEpsilonC = new JLabel("epsilon c");
        panelEpsilonC.add(lblEpsilonC);
        
        final JFormattedTextField frmtdtxtfldEpsilonC = new JFormattedTextField(doubleFormat);
        frmtdtxtfldEpsilonC.setHorizontalAlignment(SwingConstants.TRAILING);
        panelEpsilonC.add(frmtdtxtfldEpsilonC);
        frmtdtxtfldEpsilonC.setColumns(4);
        frmtdtxtfldEpsilonC.setValue((Number)2.3);
        
        JPanel panelAlphaF = new JPanel();
        FlowLayout flowLayout_8 = (FlowLayout) panelAlphaF.getLayout();
        flowLayout_8.setAlignment(FlowLayout.RIGHT);
        panelGautraisValues.add(panelAlphaF);
        
        JLabel lblAlphaF = new JLabel("alpha f");
        panelAlphaF.add(lblAlphaF);
        
        final JFormattedTextField frmtdtxtfldAlphaF = new JFormattedTextField(doubleFormat);
        frmtdtxtfldAlphaF.setHorizontalAlignment(SwingConstants.TRAILING);
        panelAlphaF.add(frmtdtxtfldAlphaF);
        frmtdtxtfldAlphaF.setColumns(4);
        frmtdtxtfldAlphaF.setValue((Number)162.3);
        
        JPanel panelBetaF = new JPanel();
        FlowLayout flowLayout_9 = (FlowLayout) panelBetaF.getLayout();
        flowLayout_9.setAlignment(FlowLayout.RIGHT);
        panelGautraisValues.add(panelBetaF);
        
        JLabel lblBetaF = new JLabel("beta f");
        panelBetaF.add(lblBetaF);
        
        final JFormattedTextField frmtdtxtfldBetaF = new JFormattedTextField(doubleFormat);
        frmtdtxtfldBetaF.setHorizontalAlignment(SwingConstants.TRAILING);
        panelBetaF.add(frmtdtxtfldBetaF);
        frmtdtxtfldBetaF.setColumns(4);
        frmtdtxtfldBetaF.setValue((Number)75.4);
        
        JPanel panelEnvironmentVariables = new JPanel();
        panelTab3.add(panelEnvironmentVariables);
        panelEnvironmentVariables.setLayout(new BoxLayout(panelEnvironmentVariables, BoxLayout.Y_AXIS));
        
        JLabel lblEnvironmentVariables = new JLabel("Environment Variables");
        lblEnvironmentVariables.setAlignmentX(Component.CENTER_ALIGNMENT);
        panelEnvironmentVariables.add(lblEnvironmentVariables);
        
        panelAngle = new JPanel();
        FlowLayout flowLayout_10 = (FlowLayout) panelAngle.getLayout();
        flowLayout_10.setAlignment(FlowLayout.RIGHT);
        panelEnvironmentVariables.add(panelAngle);
        
        JLabel lblAngle = new JLabel("Angle");
        panelAngle.add(lblAngle);
        
        final JFormattedTextField frmtdtxtfldAngle = new JFormattedTextField(doubleFormat);
        frmtdtxtfldAngle.setHorizontalAlignment(SwingConstants.TRAILING);
        frmtdtxtfldAngle.setToolTipText("Angle between destinations");
        panelAngle.add(frmtdtxtfldAngle);
        frmtdtxtfldAngle.setColumns(3);
        frmtdtxtfldAngle.setValue((Number)72.00);
        
        panelNumberOfDestinations = new JPanel();
        FlowLayout flowLayout_13 = (FlowLayout) panelNumberOfDestinations.getLayout();
        flowLayout_13.setAlignment(FlowLayout.RIGHT);
        panelNumberOfDestinations.setVisible(false);
        panelEnvironmentVariables.add(panelNumberOfDestinations);
        
        JLabel lblNumberOfDestinations = new JLabel("Number of Destinations");
        panelNumberOfDestinations.add(lblNumberOfDestinations);
        
        JFormattedTextField frmtdtxtfldNumberOfDestinations = new JFormattedTextField(countFormat);
        frmtdtxtfldNumberOfDestinations.setHorizontalAlignment(SwingConstants.TRAILING);
        panelNumberOfDestinations.add(frmtdtxtfldNumberOfDestinations);
        frmtdtxtfldNumberOfDestinations.setColumns(3);
        frmtdtxtfldNumberOfDestinations.setValue((Number)2);
        
        panelDistance = new JPanel();
        FlowLayout flowLayout_11 = (FlowLayout) panelDistance.getLayout();
        flowLayout_11.setAlignment(FlowLayout.RIGHT);
        panelEnvironmentVariables.add(panelDistance);
        
        JLabel lblDistance = new JLabel("Distance");
        panelDistance.add(lblDistance);
        
        frmtdtxtfldDistance = new JFormattedTextField(doubleFormat);
        frmtdtxtfldDistance.setHorizontalAlignment(SwingConstants.TRAILING);
        frmtdtxtfldDistance.setToolTipText("Distance the destination is from origin (0,0)");
        panelDistance.add(frmtdtxtfldDistance);
        frmtdtxtfldDistance.setColumns(3);
        frmtdtxtfldDistance.setValue((Number)150.0);
        
        panelPercentage = new JPanel();
        FlowLayout flowLayout_12 = (FlowLayout) panelPercentage.getLayout();
        flowLayout_12.setAlignment(FlowLayout.RIGHT);
        panelEnvironmentVariables.add(panelPercentage);
        
        JLabel lblPercentage = new JLabel("Percentage");
        panelPercentage.add(lblPercentage);
        
        frmtdtxtfldPercentage = new JFormattedTextField(doubleFormat);
        frmtdtxtfldPercentage.setHorizontalAlignment(SwingConstants.TRAILING);
        frmtdtxtfldPercentage.setToolTipText("The percentage moving to one of the two destinations ( The other gets 1 - percentage).");
        panelPercentage.add(frmtdtxtfldPercentage);
        frmtdtxtfldPercentage.setColumns(3);
        frmtdtxtfldPercentage.setValue((Number)0.500);
        
        panelInformedCount = new JPanel();
        panelEnvironmentVariables.add( panelInformedCount );
        
        JLabel lblInformedCount = new JLabel("Informed Count");
        panelInformedCount.add( lblInformedCount );
        
        final JFormattedTextField frmtdtxtfldInformedCount = new JFormattedTextField(countFormat);
        frmtdtxtfldInformedCount.setHorizontalAlignment(SwingConstants.TRAILING);
        frmtdtxtfldInformedCount.setColumns(3);
        frmtdtxtfldInformedCount.setToolTipText( "The number of agents moving toward a preferred destination. This number is duplicated on the southern pole as well." );
        frmtdtxtfldInformedCount.setValue( (Number)4 );
        panelInformedCount.setVisible( false );
        
        panelInformedCount.add( frmtdtxtfldInformedCount );
        
        JPanel panelStartButtons = new JPanel();
        
        JButton btnStartSimulation = new JButton("Create Simulator Instance");
        btnStartSimulation.setToolTipText("Creates a new simulator instance from the settings provided.");
        btnStartSimulation.addActionListener(new ActionListener(){
            @Override
            public void actionPerformed(ActionEvent e) {
                boolean isReady = true;
                ErrorPacketContainer errorPacketContainer = new ErrorPacketContainer();
                
                if(jframeErrorMessages != null && jframeErrorMessages.isVisible()){
                    jframeErrorMessages.dispose();
                }
                
                frmtdtxtfldRunCount.setBackground( Color.WHITE );
                frmtdtxtfldSimCount.setBackground( Color.WHITE );
                frmtdtxtfldMaxTimeSteps.setBackground( Color.WHITE );
                frmtdtxtfldPredationMinimum.setBackground( Color.WHITE );
                frmtdtxtfldPredationConstant.setBackground( Color.WHITE );
                frmtdtxtfldNearestNeighborCount.setBackground( Color.WHITE );
                frmtdtxtfldMaxLocationRadius.setBackground( Color.WHITE );
                frmtdtxtfldPercentage.setBackground( Color.WHITE );
                frmtdtxtfldDistance.setBackground( Color.WHITE );
                frmtdtxtfldDestinationRadius.setBackground( Color.WHITE );
                frmtdtxtfldAngle.setBackground( Color.WHITE );
                frmtdtxtfldInformedCount.setBackground( Color.WHITE );
                
                StringBuilder errorMessages = new StringBuilder();
                
                if(((Number)frmtdtxtfldRunCount.getValue()).intValue() <= 0){
                    errorMessages.append("Run Count must be positive\n");
                    frmtdtxtfldRunCount.setBackground( Color.YELLOW );
                    errorPacketContainer.addPacket("Run Count must be positive", frmtdtxtfldRunCount, 0);
                    isReady = false;
                }
                if(((Number)frmtdtxtfldSimCount.getValue()).intValue() <= 0){
                    errorMessages.append("Sim Count must be positive\n");
                    frmtdtxtfldSimCount.setBackground( Color.YELLOW );
                    errorPacketContainer.addPacket("Sim Count must be positive", frmtdtxtfldSimCount, 0);
                    isReady = false;
                }
                if(((Number)frmtdtxtfldMaxTimeSteps.getValue()).intValue() <= 0){
                    errorMessages.append("Max Time Steps must be positive\n");
                    frmtdtxtfldMaxTimeSteps.setBackground( Color.YELLOW );
                    errorPacketContainer.addPacket("Max Time Steps must be positive", frmtdtxtfldMaxTimeSteps, 0);
                    isReady = false;
                }
                if(((Number)frmtdtxtfldPredationMinimum.getValue()).doubleValue() < 0 && chckbxPredationEnable.isSelected()){
                    errorMessages.append("Predation Minimum must be positive\n");
                    frmtdtxtfldPredationMinimum.setBackground( Color.YELLOW );
                    errorPacketContainer.addPacket("Predation Minimum must be positive", frmtdtxtfldPredationMinimum, 1);
                    isReady = false;
                }
                if(((Number)frmtdtxtfldPredationConstant.getValue()).doubleValue() <= 0 && chckbxPredationEnable.isSelected()){
                    errorMessages.append("Predation Constant must be positive\n");
                    frmtdtxtfldPredationConstant.setBackground( Color.YELLOW );
                    errorPacketContainer.addPacket("Predation Constant must be positive", frmtdtxtfldPredationConstant, 1);
                    isReady = false;
                }
                if(((Number)frmtdtxtfldNearestNeighborCount.getValue()).intValue() < 0 && panelNearestNeighborCount.isVisible()){
                    errorMessages.append("Nearest Neighbor Count must be positive\n");
                    frmtdtxtfldNearestNeighborCount.setBackground( Color.YELLOW );
                    errorPacketContainer.addPacket("Nearest Neighbor Count must be positive", frmtdtxtfldNearestNeighborCount, 1);
                    isReady = false;
                }
                if(((Number)frmtdtxtfldMaxLocationRadius.getValue()).doubleValue() < 0 && panelMaxLocationRadius.isVisible()){
                    errorMessages.append("Max Location Radius must be positive\n");
                    frmtdtxtfldMaxLocationRadius.setBackground( Color.YELLOW );
                    errorPacketContainer.addPacket("Max Location Radius must be positive", frmtdtxtfldMaxLocationRadius, 1);
                    isReady = false;
                }
                if((((Number)frmtdtxtfldPercentage.getValue()).doubleValue() < 0.0 || ((Number)frmtdtxtfldPercentage.getValue()).doubleValue() > 1.0) && panelPercentage.isVisible()){
                    errorMessages.append("Percentage needs to be greater than or equal to 0 and less than or equal to 1\n");
                    frmtdtxtfldPercentage.setBackground( Color.YELLOW );
                    errorPacketContainer.addPacket("Percentage needs to be greater than or equal to 0 and less than or equal to 1", frmtdtxtfldPercentage, 2);
                    isReady = false;
                }
                if(((Number)frmtdtxtfldDistance.getValue()).doubleValue() <= 0 && frmtdtxtfldDistance.isVisible()){
                    errorMessages.append("Distance must be positive\n");
                    frmtdtxtfldDistance.setBackground( Color.YELLOW );
                    errorPacketContainer.addPacket("Distance must be positive", frmtdtxtfldDistance, 2);
                    isReady = false;
                }
                if(((Number)frmtdtxtfldDestinationRadius.getValue()).doubleValue() <= 0){
                    errorMessages.append("Destination Radius must be positive\n");
                    frmtdtxtfldDestinationRadius.setBackground( Color.YELLOW );
                    errorPacketContainer.addPacket("Destination Radius must be positive", frmtdtxtfldDestinationRadius, 0);
                    isReady = false;
                }
                if(((Number)frmtdtxtfldAngle.getValue()).doubleValue() < 0){
                    errorMessages.append("Angle must be positive or zero\n");
                    frmtdtxtfldAngle.setBackground( Color.YELLOW );
                    errorPacketContainer.addPacket("Angle must be positive", frmtdtxtfldAngle, 2);
                    isReady = false;
                }
                if(((Number)frmtdtxtfldInformedCount.getValue()).intValue() <= 0){
                    errorMessages.append("Informed Count must be positive\n");
                    frmtdtxtfldInformedCount.setBackground( Color.YELLOW );
                    errorPacketContainer.addPacket("Informed Count must be positive", frmtdtxtfldInformedCount, 2);
                    isReady = false;
                }
                else if(((Number)frmtdtxtfldInformedCount.getValue()).intValue() * 2 > sliderAgent.getValue()){
                    errorMessages.append("Informed Count should at most be half the count of total agents\n");
                    frmtdtxtfldInformedCount.setBackground( Color.YELLOW );
                    errorPacketContainer.addPacket("Informed Count should at most be half the count of total agents", frmtdtxtfldInformedCount, 2);
                    isReady = false;
                }
                
                
                if(!isReady){
                    jframeErrorMessages = createJFrameErrorMessages(errorPacketContainer, tabbedPane);
                    jframeErrorMessages.setVisible(true);
                }
                else{
                    _simulatorProperties = new Properties();
                    
                    _simulatorProperties.put("run-count", String.valueOf(frmtdtxtfldRunCount.getValue()));
                    _simulatorProperties.put("simulation-count", String.valueOf(frmtdtxtfldSimCount.getValue()));
                    _simulatorProperties.put("max-simulation-time-steps", String.valueOf(frmtdtxtfldMaxTimeSteps.getValue()));
                    _simulatorProperties.put("random-seed", String.valueOf(1)); // Doesn't change
                    _simulatorProperties.put("individual-count", String.valueOf(sliderAgent.getValue()));
                    _simulatorProperties.put("run-graphical", String.valueOf(chckbxGraphical.isSelected()));
                    _simulatorProperties.put("pre-calculate-probabilities", String.valueOf(false)); // Doesn't change
                    _simulatorProperties.put("use-random-random-seed", String.valueOf(chckbxRandomSeed.isSelected()));
                    _simulatorProperties.put("can-multiple-initiate", String.valueOf(true)); // Doesn't change
                    
                    _simulatorProperties.put("eskridge-results", String.valueOf(chckbxEskridge.isSelected()));
                    _simulatorProperties.put("conflict-results", String.valueOf(chckbxConflict.isSelected()));
                    _simulatorProperties.put("position-results", String.valueOf(chckbxPosition.isSelected()));
                    _simulatorProperties.put("predation-results", String.valueOf(chckbxPredationResults.isSelected()));
                    
                    _simulatorProperties.put("communication-type", String.valueOf(comboBoxCommType.getSelectedItem()).toLowerCase());
                    
                    _simulatorProperties.put("nearest-neighbor-count", String.valueOf(frmtdtxtfldNearestNeighborCount.getValue()));
                    _simulatorProperties.put("max-location-radius", String.valueOf(frmtdtxtfldMaxLocationRadius.getValue()));
                    
                    _simulatorProperties.put("destination-size-radius", String.valueOf(frmtdtxtfldDestinationRadius.getValue()));
                    
                    _simulatorProperties.put("max-agents-eaten-per-step", String.valueOf(spinnerMaxEaten.getValue()));
                    _simulatorProperties.put("enable-predator", String.valueOf(chckbxPredationEnable.isSelected()));
                    _simulatorProperties.put("predation-probability-minimum", String.valueOf( frmtdtxtfldPredationMinimum.getValue() ));
                    _simulatorProperties.put("predation-multiplier", String.valueOf(frmtdtxtfldPredationConstant.getValue()));
                    _simulatorProperties.put("use-predation-threshold", String.valueOf(chckbxUsePredationThreshold.isSelected()));
                    _simulatorProperties.put("predation-threshold", String.valueOf(spinnerPredationThreshold.getValue()));
                    _simulatorProperties.put("predation-by-population", String.valueOf(chckbxPopulationIndependent.isSelected()));
                    _simulatorProperties.put("count-non-movers-as-survivors", String.valueOf( chckbxNonMoversSurvive.isSelected() ));
                    
                    _simulatorProperties.put( "stop-at-any-destination", String.valueOf( chckbxStopAnywhere.isSelected() ) );
                    
                    _simulatorProperties.put("adhesion-time-limit", String.valueOf(frmtdtxtfldMaxTimeSteps.getValue()));
                    
                    _simulatorProperties.put("alpha", String.valueOf(frmtdtxtfldAlpha.getValue()));
                    _simulatorProperties.put("alpha-c", String.valueOf(frmtdtxtfldAlphaC.getValue()));
                    _simulatorProperties.put("beta", String.valueOf(frmtdtxtfldBeta.getValue()));
                    _simulatorProperties.put("beta-c", String.valueOf(frmtdtxtfldBetaC.getValue()));
                    _simulatorProperties.put("S", String.valueOf(frmtdtxtfldS.getValue()));
                    _simulatorProperties.put("q", String.valueOf(frmtdtxtfldQ.getValue()));
                    
                    _simulatorProperties.put("lambda", String.valueOf(0.2));
                    
                    _simulatorProperties.put("tau-o", String.valueOf(frmtdtxtfldTaoO.getValue()));
                    _simulatorProperties.put("gamma-c", String.valueOf(frmtdtxtfldGammaC.getValue()));
                    _simulatorProperties.put("epsilon-c", String.valueOf(frmtdtxtfldEpsilonC.getValue()));
                    _simulatorProperties.put("alpha-f", String.valueOf(frmtdtxtfldAlphaF.getValue()));
                    _simulatorProperties.put("beta-f", String.valueOf(frmtdtxtfldBetaF.getValue()));
                    
                    _simulatorProperties.put("default-conflict-value", String.valueOf(spinnerDefaultConflict.getValue()));
                    
                    _simulatorProperties.put("cancellation-threshold", String.valueOf(spinnerCancelationThreshold.getValue()));
                    StringBuilder sbAgentBuilder = new StringBuilder();
                    sbAgentBuilder.append("edu.snu.leader.discrete.simulator.Sueur");
                    sbAgentBuilder.append(comboBoxAgentBuilder.getSelectedItem().toString().replace(" ", ""));
                    sbAgentBuilder.append("AgentBuilder");
                    _simulatorProperties.put("agent-builder", String.valueOf(sbAgentBuilder.toString()));
                    
                    StringBuilder sbDecisionCalculator = new StringBuilder();
                    sbDecisionCalculator.append("edu.snu.leader.discrete.simulator.");
                    sbDecisionCalculator.append(comboBoxModel.getSelectedItem());
//                    sbDecisionCalculator.append(comboBoxDecisionCalculator.getSelectedItem());
                    sbDecisionCalculator.append(comboBoxDecisionCalculator.getSelectedItem().toString().replace(" ", ""));
                    sbDecisionCalculator.append("DecisionCalculator");
                    _simulatorProperties.put("decision-calculator", String.valueOf(sbDecisionCalculator.toString()));
                    
                    StringBuilder sbLocationsFile = new StringBuilder();
                    sbLocationsFile.append("cfg/sim/locations/metric/valid-metric-loc-");
                    sbLocationsFile.append(String.format("%03d",sliderAgent.getValue()));
                    sbLocationsFile.append("-seed-00001.dat");
                    _simulatorProperties.put("locations-file", String.valueOf(sbLocationsFile.toString()));
                    
                    //create destination file
                    DestinationBuilder db = new DestinationBuilder(sliderAgent.getValue() , 1L);
                    
                    StringBuilder sbDestinationsFile = new StringBuilder();
                    sbDestinationsFile.append("cfg/sim/destinations/destinations-");
                    switch(comboBoxEnvironment.getSelectedItem().toString()){
                        case("Minimum"):
                            sbDestinationsFile.append("diffdis-" + sliderAgent.getValue());
                            sbDestinationsFile.append("-per-" + frmtdtxtfldPercentage.getValue());
                            sbDestinationsFile.append("-seed-1.dat");
                            db.generateDifferentDistance(((Number)frmtdtxtfldPercentage.getValue()).doubleValue(), 200, 100, 75 );
                            break;
                        case("Medium"):
                            sbDestinationsFile.append("split-" + sliderAgent.getValue());
                            sbDestinationsFile.append("-dis-" + frmtdtxtfldDistance.getValue());
                            sbDestinationsFile.append("-ang-" + String.format("%.2f", ((Number)frmtdtxtfldAngle.getValue()).doubleValue()));
                            sbDestinationsFile.append("-per-" + String.format("%.3f", ((Number)frmtdtxtfldPercentage.getValue()).doubleValue()));
                            sbDestinationsFile.append("-seed-1.dat");
                            db.generateSplitNorth( ((Number)frmtdtxtfldDistance.getValue()).doubleValue(), ((Number)frmtdtxtfldAngle.getValue()).doubleValue(), ((Number)frmtdtxtfldPercentage.getValue()).doubleValue() );
                            break;
                        case("Maximum"):
                            sbDestinationsFile.append("poles-" + sliderAgent.getValue());
                            sbDestinationsFile.append("-per-" + frmtdtxtfldPercentage.getValue());
                            sbDestinationsFile.append("-seed-1.dat");
                            db.generatePoles( 50, 100, ((Number)frmtdtxtfldPercentage.getValue()).doubleValue() );
                            break;
                        case("Uninformed"):
                            sbDestinationsFile.append("split-poles-" + frmtdtxtfldInformedCount.getValue());
                            sbDestinationsFile.append("-dis-" +  String.format("%.1f", ((Number)frmtdtxtfldDistance.getValue()).doubleValue()));
                            sbDestinationsFile.append("-ang-" + String.format("%.2f", ((Number)frmtdtxtfldAngle.getValue()).doubleValue()));
                            sbDestinationsFile.append("-per-" + String.format("%.3f", ((Number)frmtdtxtfldPercentage.getValue()).doubleValue()));
                            sbDestinationsFile.append("-seed-1.dat");
                            db.generateSplitPoles( ((Number)frmtdtxtfldDistance.getValue()).doubleValue(), ((Number)frmtdtxtfldAngle.getValue()).doubleValue(), ((Number)frmtdtxtfldPercentage.getValue()).doubleValue(), ((Number)frmtdtxtfldInformedCount.getValue()).intValue() );
                            break;
                        default: //Should never happen
                            break;
                    }
                    _simulatorProperties.put("destinations-file", String.valueOf(sbDestinationsFile.toString()));
                    
                    _simulatorProperties.put("live-delay", String.valueOf(15)); //Doesn't change
                    _simulatorProperties.put("results-dir", "results"); //Doesn't change
                    
                    new Thread(new Runnable() {
                        public void run(){
                            try{
                                runSimulation();
                            }
                            catch(Exception e){
                                e.printStackTrace();
                            }
                        }
                    }).start();
                }
            }
            
        });
        panelStartButtons.add(btnStartSimulation);
        
        JButton btnStartSimulationFrom = new JButton("Run Simulation from Properties File");
        btnStartSimulationFrom.setToolTipText("Runs the simulator with the values provided in the properties file.");
        btnStartSimulationFrom.setEnabled(false);
        panelStartButtons.add(btnStartSimulationFrom);
        
        panelTab3.add(panelStartButtons);
    }
    
    private void runSimulation(){
        String stringShouldRunGraphical = _simulatorProperties.getProperty( "run-graphical" );
        Validate.notEmpty( stringShouldRunGraphical, "Run graphical option required" );
        boolean shouldRunGraphical = Boolean.parseBoolean( stringShouldRunGraphical );
        
        String stringTotalRuns = _simulatorProperties.getProperty( "run-count" );
        Validate.notEmpty( stringTotalRuns, "Run count required" );
        int totalRuns = Integer.parseInt( stringTotalRuns );
        
        if(!shouldRunGraphical){
            // run just text
            for( int run = 1; run <= totalRuns; run++ )
            {
                System.out.println( "Run " + run );
                System.out.println();
                Simulator simulator = new Simulator( run );
                _simulatorProperties.setProperty( "current-run", String.valueOf(run) );
                simulator.initialize(_simulatorProperties);
                simulator.execute();
            }
        }
        else{
            // run graphical
            DebugLocationsStructure db = new DebugLocationsStructure(
                    "Conflict Simulation", 800, 600, 60 );
            _simulatorProperties.setProperty( "current-run", String.valueOf(1) );
            db.initialize( _simulatorProperties, 1 );
            db.setLocation( this.getX() + this.getWidth() + 10, this.getY() );
            db.run();
        }
    }
    
    public class ErrorPacketContainer{
        public List<String> errorMessages = new ArrayList<String>();
        public List<Component> erroredComponents = new ArrayList<Component>();
        public List<Integer> tabIndexes = new ArrayList<Integer>();
        
        public void addPacket(String errorMessage, Component errorComponent, int tabIndex){
            errorMessages.add(errorMessage);
            erroredComponents.add(errorComponent);
            tabIndexes.add(tabIndex);
        }
    }
    
    JFrame createJFrameErrorMessages(ErrorPacketContainer errorPackets, JTabbedPane theTabbedPane){
        JFrame errorMessages = new JFrame();
        JPanel errorMessagesContentPane = new JPanel();
        
        errorMessages.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        errorMessages.setTitle("Error Messages");
        errorMessages.setBounds(this.getX() + this.getWidth() + 5, this.getY(), 350, 300);
        errorMessagesContentPane = new JPanel();
        errorMessagesContentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
        errorMessagesContentPane.setLayout(new BorderLayout(0, 0));
        errorMessages.setContentPane(errorMessagesContentPane);
        
        JPanel panel = new JPanel();
        errorMessagesContentPane.add(panel, BorderLayout.NORTH);
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        
        for(int i = 0; i < errorPackets.errorMessages.size(); i++){
            JTextPane temp = createErrorPane(errorPackets.errorMessages.get(i), errorPackets.erroredComponents.get(i), theTabbedPane, errorPackets.tabIndexes.get(i));
            panel.add(temp);
        }
        
        return errorMessages;
    }
    
    JTextPane createErrorPane(String errorMessage, Component component, final JTabbedPane tabbedPane, final int tabIndex){
        final JTextPane errorPane = new JTextPane();
        
        errorPane.setEditable(false);
        errorPane.setText(errorMessage);
        errorPane.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent arg0) {
                errorPane.setBackground(Color.YELLOW);
                tabbedPane.setSelectedIndex(tabIndex);
            }
            
            @Override
            public void focusLost(FocusEvent arg0) {
                errorPane.setBackground(Color.WHITE);
            }
        });
        
        component.requestFocusInWindow();
        
        return errorPane;
    }
}
