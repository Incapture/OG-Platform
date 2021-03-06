/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.integration.tool.portfolio;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.component.tool.AbstractTool;
import com.opengamma.financial.tool.ToolContext;
import com.opengamma.integration.tool.IntegrationToolContext;
import com.opengamma.util.generate.scripts.Scriptable;

/**
 * Tool to delete positions that are not currently in a portfolio
 */
@Scriptable
public class OrphanedPositionDeleteTool extends AbstractTool<IntegrationToolContext> {
  
  private static final Logger s_logger = LoggerFactory.getLogger(OrphanedPositionDeleteTool.class);
  /**
   * Main method to run the tool.
   */
  public static void main(String[] args) { // CSIGNORE
    new OrphanedPositionDeleteTool().initAndRun(args, IntegrationToolContext.class);
    System.exit(0);
  }

  @Override
  protected void doRun() throws Exception {
    ToolContext toolContext = getToolContext();
    OrphanedPositionRemover orphanedPositionRemover = new OrphanedPositionRemover(toolContext.getPortfolioMaster(), toolContext.getPositionMaster());
    s_logger.info("running orphanedPositionRemover");
    orphanedPositionRemover.run();
    toolContext.close();
  }
  
}
