package org.activiti.pm;

import java.io.Serializable;

import org.activiti.engine.history.HistoricActivityInstance;
import org.activiti.engine.impl.pvm.PvmTransition;
import org.activiti.engine.impl.pvm.process.ActivityImpl;

/**
 * Represents historic activity instance with flow
 *
 * @author iam
 *
 */
public class HistoricActivityInstanceFlow implements Serializable {

  private HistoricActivityInstance historicActivityInstance;
  private PvmTransition incomingTransition;
  private ActivityImpl activity;

  public HistoricActivityInstance getHistoricActivityInstance() {
    return historicActivityInstance;
  }

  public void setHistoricActivityInstance(HistoricActivityInstance historicActivityInstance) {
    this.historicActivityInstance = historicActivityInstance;
  }

  public PvmTransition getIncomingTransition() {
    return incomingTransition;
  }

  public void setIncomingTransition(PvmTransition incomingTransition) {
    this.incomingTransition = incomingTransition;
  }

  public ActivityImpl getActivity() {
    return activity;
  }

  public void setActivity(ActivityImpl activity) {
    this.activity = activity;
  }

  public String getIncomingTransitionId() {
    if (incomingTransition == null) {
      return null;
    }
    return incomingTransition.getId();
  }

  public String getIncomingTransitionName() {
    if (incomingTransition == null) {
      return null;
    }
    return String.valueOf(incomingTransition.getProperty("name"));
  }

  @Override
  public String toString() {
    // (startevent1)--flow1-->
    StringBuilder sb = new StringBuilder();
    if (incomingTransition != null) {
      sb.append(incomingTransition.getSource().toString());
      sb.append("--");
      sb.append(incomingTransition.getId());
      sb.append("-->");
    }
    sb.append(historicActivityInstance.toString());
    return sb.toString();
  }
}
