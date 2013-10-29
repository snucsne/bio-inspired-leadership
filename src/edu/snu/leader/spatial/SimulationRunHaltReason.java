/*
 * COPYRIGHT
 */
package edu.snu.leader.spatial;

/**
 * SimulationRunHaltReason
 *
 * TODO Class description
 *
 * @author Brent Eskridge
 * @version $Revision$ ($Author$)
 */
public enum SimulationRunHaltReason {
    ALL_AGENTS_DEPARTED,
    ONLY_INITIATOR_CANCELED,
    MAX_SIM_RUN_STEPS_REACHED,
    ADHESION_LIMIT_REACHED;
}
