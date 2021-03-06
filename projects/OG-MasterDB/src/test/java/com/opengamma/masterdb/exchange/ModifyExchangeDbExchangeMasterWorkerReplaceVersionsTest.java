/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.masterdb.exchange;

import static com.google.common.collect.Lists.newArrayList;
import static org.testng.AssertJUnit.assertEquals;

import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.time.Instant;
import javax.time.TimeSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;

import com.opengamma.id.ExternalIdBundle;
import com.opengamma.id.ObjectId;
import com.opengamma.id.VersionCorrection;
import com.opengamma.master.exchange.ExchangeDocument;
import com.opengamma.master.exchange.ExchangeHistoryRequest;
import com.opengamma.master.exchange.ExchangeHistoryResult;
import com.opengamma.master.exchange.ExchangeSearchRequest;
import com.opengamma.master.exchange.ExchangeSearchResult;
import com.opengamma.master.exchange.ManageableExchange;
import com.opengamma.util.test.DbTest;

/**
 * Tests ModifyExchangeDbExchangeMasterWorker.
 */
public class ModifyExchangeDbExchangeMasterWorkerReplaceVersionsTest extends AbstractDbExchangeMasterWorkerTest {
  // superclass sets up dummy database

  private static final Logger s_logger = LoggerFactory.getLogger(ModifyExchangeDbExchangeMasterWorkerReplaceVersionsTest.class);

  @Factory(dataProvider = "databases", dataProviderClass = DbTest.class)
  public ModifyExchangeDbExchangeMasterWorkerReplaceVersionsTest(String databaseType, String databaseVersion) {
    super(databaseType, databaseVersion, false);
    s_logger.info("running testcases for {}", databaseType);
  }

  //-------------------------------------------------------------------------  

  @Test
  public void test_ReplaceVersion_of_some_middle_version() {
    TimeSource origTimeSource = _exgMaster.getTimeSource();
    try {
      Instant now = Instant.now();

      ObjectId baseOid = setupTestData(now);
      _exgMaster.setTimeSource(TimeSource.fixed(now.plus(2, TimeUnit.HOURS)));
      ExchangeDocument latestDoc = _exgMaster.get(baseOid, VersionCorrection.LATEST);
      Instant latestFrom = latestDoc.getVersionFromInstant();

      final ExternalIdBundle bundle = ExternalIdBundle.of("B", "B0");
      final ExternalIdBundle region = ExternalIdBundle.of("R", "R0");
      List<ExchangeDocument> replacement = newArrayList();
      for (int i = 0; i <= 10; i++) {
        ManageableExchange ex = new ManageableExchange(bundle, "test" + i, region, null);
        ExchangeDocument doc = new ExchangeDocument(ex);
        doc.setVersionFromInstant(latestFrom.plus(i, TimeUnit.MINUTES));
        replacement.add(doc);
      }

      _exgMaster.replaceVersion(latestDoc.getUniqueId(), replacement);

      ExchangeSearchRequest searchRequest = new ExchangeSearchRequest();
      searchRequest.addExternalIds(bundle.getExternalIds());
      searchRequest.setVersionCorrection(VersionCorrection.LATEST);
      ExchangeSearchResult result = _exgMaster.search(searchRequest);
      List<ManageableExchange> exchanges = result.getExchanges();

      assertEquals(1, exchanges.size());
      ManageableExchange ex = exchanges.get(0);
      assertEquals("test10", ex.getName());

    } finally {
      _exgMaster.setTimeSource(origTimeSource);
    }
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void test_ReplaceVersion_of_some_middle_version_timeBoundsNotExact() {
    TimeSource origTimeSource = _exgMaster.getTimeSource();
    try {
      Instant now = Instant.now();

      ObjectId baseOid = setupTestData(now);
      _exgMaster.setTimeSource(TimeSource.fixed(now.plus(2, TimeUnit.HOURS)));
      ExchangeDocument latestDoc = _exgMaster.get(baseOid, VersionCorrection.LATEST);
      Instant latestFrom = latestDoc.getVersionFromInstant();

      final ExternalIdBundle bundle = ExternalIdBundle.of("B", "B0");
      final ExternalIdBundle region = ExternalIdBundle.of("R", "R0");
      List<ExchangeDocument> replacement = newArrayList();
      for (int i = 1; i <= 10; i++) {
        ManageableExchange ex = new ManageableExchange(bundle, "test" + i, region, null);
        ExchangeDocument doc = new ExchangeDocument(ex);
        doc.setVersionFromInstant(latestFrom.plus(i, TimeUnit.MINUTES));
        replacement.add(doc);
      }

      _exgMaster.replaceVersion(latestDoc.getUniqueId(), replacement);

      ExchangeSearchRequest searchRequest = new ExchangeSearchRequest();
      searchRequest.addExternalIds(bundle.getExternalIds());
      searchRequest.setVersionCorrection(VersionCorrection.LATEST);
      ExchangeSearchResult result = _exgMaster.search(searchRequest);
      List<ManageableExchange> exchanges = result.getExchanges();

      assertEquals(1, exchanges.size());
      ManageableExchange ex = exchanges.get(0);
      assertEquals("test10", ex.getName());

    } finally {
      _exgMaster.setTimeSource(origTimeSource);
    }
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void test_ReplaceVersion_which_is_not_current() {
    TimeSource origTimeSource = _exgMaster.getTimeSource();
    try {
      Instant now = Instant.now();

      ObjectId baseOid = setupTestData(now);
      _exgMaster.setTimeSource(TimeSource.fixed(now.plus(2, TimeUnit.HOURS)));


      ExchangeHistoryRequest historyRequest = new ExchangeHistoryRequest();
      historyRequest.setObjectId(baseOid);
      historyRequest.setCorrectionsToInstant(null);
      ExchangeHistoryResult result = _exgMaster.history(historyRequest);
      List<ExchangeDocument> exchanges = result.getDocuments();

      ExchangeDocument lastButOneDoc = exchanges.get(exchanges.size() - 1);
      Instant lastButOneDocVersionFrom = lastButOneDoc.getVersionFromInstant();

      final ExternalIdBundle bundle = ExternalIdBundle.of("B", "B0");
      final ExternalIdBundle region = ExternalIdBundle.of("R", "R0");
      List<ExchangeDocument> replacement = newArrayList();
      for (int i = 1; i <= 10; i++) {
        ManageableExchange ex = new ManageableExchange(bundle, "test" + i, region, null);
        ExchangeDocument doc = new ExchangeDocument(ex);
        doc.setVersionFromInstant(lastButOneDocVersionFrom.plus(i, TimeUnit.MINUTES));
        replacement.add(doc);
      }

      _exgMaster.replaceVersion(lastButOneDoc.getUniqueId(), replacement);

    } finally {
      _exgMaster.setTimeSource(origTimeSource);
    }
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void test_ReplaceVersion_which_is_not_in_the_time_bounds_of_the_replaced_doc() {
    TimeSource origTimeSource = _exgMaster.getTimeSource();
    try {
      Instant now = Instant.now();

      ObjectId baseOid = setupTestData(now);
      _exgMaster.setTimeSource(TimeSource.fixed(now.plus(2, TimeUnit.HOURS)));


      ExchangeHistoryRequest historyRequest = new ExchangeHistoryRequest();
      historyRequest.setObjectId(baseOid);
      historyRequest.setCorrectionsToInstant(null);
      ExchangeHistoryResult result = _exgMaster.history(historyRequest);
      List<ExchangeDocument> exchanges = result.getDocuments();

      ExchangeDocument lastButOneDoc = exchanges.get(exchanges.size() - 3);
      Instant lastButOneDocVersionFrom = lastButOneDoc.getVersionFromInstant();

      final ExternalIdBundle bundle = ExternalIdBundle.of("B", "B0");
      final ExternalIdBundle region = ExternalIdBundle.of("R", "R0");
      List<ExchangeDocument> replacement = newArrayList();
      for (int i = 1; i <= 10; i++) {
        ManageableExchange ex = new ManageableExchange(bundle, "test" + i, region, null);
        ExchangeDocument doc = new ExchangeDocument(ex);
        doc.setVersionFromInstant(lastButOneDocVersionFrom.plus(i, TimeUnit.MINUTES));
        replacement.add(doc);
      }

      _exgMaster.replaceVersion(lastButOneDoc.getUniqueId(), replacement);

    } finally {
      _exgMaster.setTimeSource(origTimeSource);
    }
  }

  @Test
  public void test_ReplaceVersions() {
    TimeSource origTimeSource = _exgMaster.getTimeSource();
    try {
      Instant now = Instant.now();

      ObjectId baseOid = setupTestData(now);
      _exgMaster.setTimeSource(TimeSource.fixed(now.plus(2, TimeUnit.HOURS)));
      ExchangeDocument latestDoc = _exgMaster.get(baseOid, VersionCorrection.LATEST);
      Instant latestFrom = latestDoc.getVersionFromInstant();

      final ExternalIdBundle bundle = ExternalIdBundle.of("B", "B0");
      final ExternalIdBundle region = ExternalIdBundle.of("R", "R0");
      List<ExchangeDocument> replacement = newArrayList();
      for (int i = 0; i <= 10; i++) {
        ManageableExchange ex = new ManageableExchange(bundle, "test" + i, region, null);
        ExchangeDocument doc = new ExchangeDocument(ex);
        doc.setVersionFromInstant(latestFrom.plus(i, TimeUnit.SECONDS));
        replacement.add(doc);
      }

      _exgMaster.replaceVersions(latestDoc, replacement);

      ExchangeHistoryRequest historyRequest = new ExchangeHistoryRequest();
      historyRequest.setObjectId(baseOid);
      historyRequest.setCorrectionsFromInstant(now.plus(2, TimeUnit.HOURS));
      ExchangeHistoryResult result = _exgMaster.history(historyRequest);
      List<ExchangeDocument> exchanges = result.getDocuments();

      assertEquals(15, exchanges.size());

    } finally {
      _exgMaster.setTimeSource(origTimeSource);
    }
  }

  @Test
  public void test_ReplaceVersions2() {
    TimeSource origTimeSource = _exgMaster.getTimeSource();
    try {
      Instant now = Instant.now();

      ObjectId baseOid = setupTestData(now);
      _exgMaster.setTimeSource(TimeSource.fixed(now.plus(2, TimeUnit.HOURS)));
      ExchangeDocument latestDoc = _exgMaster.get(baseOid, VersionCorrection.LATEST);
      Instant latestFrom = latestDoc.getVersionFromInstant();

      final ExternalIdBundle bundle = ExternalIdBundle.of("B", "B0");
      final ExternalIdBundle region = ExternalIdBundle.of("R", "R0");
      List<ExchangeDocument> replacement = newArrayList();
      for (int i = 0; i <= 10; i++) {
        ManageableExchange ex = new ManageableExchange(bundle, "test" + i, region, null);
        ExchangeDocument doc = new ExchangeDocument(ex);
        doc.setVersionFromInstant(latestFrom.plus(i - 3, TimeUnit.MINUTES));
        replacement.add(doc);
      }

      _exgMaster.replaceVersions(latestDoc, replacement);

      ExchangeHistoryRequest historyRequest = new ExchangeHistoryRequest();
      historyRequest.setObjectId(baseOid);
      historyRequest.setCorrectionsFromInstant(now.plus(2, TimeUnit.HOURS));
      ExchangeHistoryResult result = _exgMaster.history(historyRequest);
      List<ExchangeDocument> exchanges = result.getDocuments();

      assertEquals(12, exchanges.size());

    } finally {
      _exgMaster.setTimeSource(origTimeSource);
    }
  }

  @Test
  /**
   *
   *       |                        |             |
   *       |                        |             |
   *       |                        |             |                           
   *       |                        |             |                           
   *       |                        |             |                           
   *       |                        |             |                           
   *       |                        |             |                           
   *       |                        |             |                           
   *       |                        |             |                           
   *       |                        |             |                           
   *       |                        |             |                           
   *       |                        |             |                           
   *       |                        |             |                           
   *       |                        |             |                           
   *       |                        |             |                           
   *       |     setup_4            |             |                           
   *       |                        |             |                           
   *   +5m |------------------------|             |                           
   *       |                        |             |                           
   *       |     setup_3            |             |                           
   *       |                        |             |                           
   *   +4m |------------------------|             |                           
   *       |                        |             |      replace_4            
   *       |     setup_2            |  <-- +3m20s |----------------------------------->>>   
   *       |                        |             |      replace_3            
   *   +3m |------------------------|  <-- +3m00s |----------------------------------->>>   
   *       |                        |             |      replace_2            
   *       |                        |  <-- +2m40s |----------------------------------->>>   
   *       |     setup_1            |             |      replace_1            
   *       |                        |  <-- +2m20s |----------------------------------->>>   
   *       |                        |
   *       |                        |                       setup_1 (copy)
   *   +2m |------------------------ ... --------------------------------------------->>>                                                                              
   *       |                                                               
   *       |     setup_0                                   setup_0 (continuation)
   *       |                         
   *   +1m |------------------------ ... --------------------------------------------->>>                                       
   *
   *
   *   NOW =================================================================================
   *
   */
  public void test_ReplaceVersions3() {
    TimeSource origTimeSource = _exgMaster.getTimeSource();
    try {
      Instant now = Instant.now();

      ObjectId baseOid = setupTestData(now);
      _exgMaster.setTimeSource(TimeSource.fixed(now.plus(2, TimeUnit.HOURS)));
      ExchangeDocument latestDoc = _exgMaster.get(baseOid, VersionCorrection.LATEST);
      Instant latestFrom = latestDoc.getVersionFromInstant();

      final ExternalIdBundle bundle = ExternalIdBundle.of("B", "B0");
      final ExternalIdBundle region = ExternalIdBundle.of("R", "R0");
      List<ExchangeDocument> replacement = newArrayList();
      for (int i = 1; i <= 4; i++) {
        ManageableExchange ex = new ManageableExchange(bundle, "replace_" + i, region, null);
        ExchangeDocument doc = new ExchangeDocument(ex);
        doc.setVersionFromInstant(now.plus(1, TimeUnit.MINUTES).plus(i * 20, TimeUnit.SECONDS));
        replacement.add(doc);
      }

      _exgMaster.replaceVersions(latestDoc, replacement);

      ExchangeHistoryRequest historyRequest = new ExchangeHistoryRequest();
      historyRequest.setObjectId(baseOid);
      historyRequest.setCorrectionsFromInstant(now.plus(2, TimeUnit.HOURS));
      ExchangeHistoryResult result = _exgMaster.history(historyRequest);
      List<ExchangeDocument> exchanges = result.getDocuments();

      assertEquals(6, exchanges.size());

      latestDoc = _exgMaster.get(baseOid, VersionCorrection.LATEST);
      latestFrom = latestDoc.getVersionFromInstant();
      assertEquals(now.plus(2, TimeUnit.MINUTES).plus(20, TimeUnit.SECONDS), latestFrom);

      assertEquals(now, exchanges.get(5).getVersionFromInstant());
      assertEquals(now.plus(1, TimeUnit.MINUTES), exchanges.get(5).getVersionToInstant());
      //
      assertEquals(now.plus(1, TimeUnit.MINUTES), exchanges.get(4).getVersionFromInstant());
      assertEquals(now.plus(1, TimeUnit.MINUTES).plus(20, TimeUnit.SECONDS), exchanges.get(4).getVersionToInstant());
      //
      assertEquals(now.plus(1, TimeUnit.MINUTES).plus(20, TimeUnit.SECONDS), exchanges.get(3).getVersionFromInstant());
      assertEquals(now.plus(1, TimeUnit.MINUTES).plus(40, TimeUnit.SECONDS), exchanges.get(3).getVersionToInstant());
      //
      assertEquals(now.plus(1, TimeUnit.MINUTES).plus(40, TimeUnit.SECONDS), exchanges.get(2).getVersionFromInstant());
      assertEquals(now.plus(1, TimeUnit.MINUTES).plus(60, TimeUnit.SECONDS), exchanges.get(2).getVersionToInstant());
      //
      assertEquals(now.plus(1, TimeUnit.MINUTES).plus(60, TimeUnit.SECONDS), exchanges.get(1).getVersionFromInstant());
      assertEquals(now.plus(1, TimeUnit.MINUTES).plus(80, TimeUnit.SECONDS), exchanges.get(1).getVersionToInstant());
      //
      assertEquals(now.plus(1, TimeUnit.MINUTES).plus(80, TimeUnit.SECONDS), exchanges.get(0).getVersionFromInstant());
      assertEquals(null, exchanges.get(0).getVersionToInstant());
    } finally {
      _exgMaster.setTimeSource(origTimeSource);
    }
  }

  @Test
  /**
   *
   *       |                                      
   *       |                                      
   *       |                                                                 
   *       |                                                                 
   *       |                                                                 
   *       |                                                                 
   *       |                                                                 
   *       |                                                                 
   *       |                                                                 
   *       |                                                                 
   *       |                                                                 
   *       |                                                                 
   *       |                                                                 
   *       |                                                                 
   *       |                                                                 
   *       |     setup_4                                                     
   *       |                                                                 
   *   +5m |------------------------ ... --------------------------------------------->>>                                        
   *       |                                                                 
   *       |     setup_3                                                     
   *       |                                                                 
   *   +4m |------------------------ ... --------------------------------------------->>>        
   *       |                        |                      setup_2 (copy)
   *       |                        |
   *       |                        |  <-- +3m40s |----------------------------------->>>
   *       |                        |             |      replace_4            
   *       |     setup_2            |  <-- +3m20s |----------------------------------->>>   
   *       |                        |             |      replace_3            
   *   +3m |------------------------|  <-- +3m00s |----------------------------------->>>   
   *       |                        |             |      replace_2            
   *       |                        |  <-- +2m40s |----------------------------------->>>   
   *       |     setup_1            |             |      replace_1            
   *       |                        |  <-- +2m20s |----------------------------------->>>   
   *       |                        |
   *       |                        |                      setup_1 (copy)
   *   +2m |------------------------ ... --------------------------------------------->>>                                                                              
   *       |                                                               
   *       |     setup_0                                   setup_0 (continuation)
   *       |                         
   *   +1m |------------------------ ... --------------------------------------------->>>                                       
   *
   *
   *   NOW =================================================================================
   *
   */
  public void test_ReplaceVersions4() {
    TimeSource origTimeSource = _exgMaster.getTimeSource();
    try {
      Instant now = Instant.now();

      ObjectId baseOid = setupTestData(now);
      _exgMaster.setTimeSource(TimeSource.fixed(now.plus(2, TimeUnit.HOURS)));
      ExchangeDocument latestDoc = _exgMaster.get(baseOid, VersionCorrection.LATEST);
      Instant latestFrom = latestDoc.getVersionFromInstant();

      final ExternalIdBundle bundle = ExternalIdBundle.of("B", "B0");
      final ExternalIdBundle region = ExternalIdBundle.of("R", "R0");
      List<ExchangeDocument> replacement = newArrayList();
      for (int i = 1; i <= 4; i++) {
        ManageableExchange ex = new ManageableExchange(bundle, "replace_" + i, region, null);
        ExchangeDocument doc = new ExchangeDocument(ex);
        doc.setVersionFromInstant(now.plus(1, TimeUnit.MINUTES).plus(i * 20, TimeUnit.SECONDS));
        replacement.add(doc);
      }
      replacement.get(replacement.size() - 1).setVersionToInstant(now.plus(1, TimeUnit.MINUTES).plus(100, TimeUnit.SECONDS));

      _exgMaster.replaceVersions(latestDoc, replacement);

      ExchangeHistoryRequest historyRequest = new ExchangeHistoryRequest();
      historyRequest.setObjectId(baseOid);
      historyRequest.setCorrectionsFromInstant(now.plus(2, TimeUnit.HOURS));
      ExchangeHistoryResult result = _exgMaster.history(historyRequest);
      List<ExchangeDocument> exchanges = result.getDocuments();

      assertEquals(9, exchanges.size());

      assertEquals(now, exchanges.get(8).getVersionFromInstant());
      assertEquals(now.plus(1, TimeUnit.MINUTES), exchanges.get(8).getVersionToInstant());
      //
      assertEquals(now.plus(1, TimeUnit.MINUTES), exchanges.get(7).getVersionFromInstant());
      assertEquals(now.plus(1, TimeUnit.MINUTES).plus(20, TimeUnit.SECONDS), exchanges.get(7).getVersionToInstant());
      //
      assertEquals(now.plus(1, TimeUnit.MINUTES).plus(20, TimeUnit.SECONDS), exchanges.get(6).getVersionFromInstant());
      assertEquals(now.plus(1, TimeUnit.MINUTES).plus(40, TimeUnit.SECONDS), exchanges.get(6).getVersionToInstant());
      //
      assertEquals(now.plus(1, TimeUnit.MINUTES).plus(40, TimeUnit.SECONDS), exchanges.get(5).getVersionFromInstant());
      assertEquals(now.plus(1, TimeUnit.MINUTES).plus(60, TimeUnit.SECONDS), exchanges.get(5).getVersionToInstant());
      //
      assertEquals(now.plus(1, TimeUnit.MINUTES).plus(60, TimeUnit.SECONDS), exchanges.get(4).getVersionFromInstant());
      assertEquals(now.plus(1, TimeUnit.MINUTES).plus(80, TimeUnit.SECONDS), exchanges.get(4).getVersionToInstant());
      //
      assertEquals(now.plus(1, TimeUnit.MINUTES).plus(80, TimeUnit.SECONDS), exchanges.get(3).getVersionFromInstant());
      assertEquals(now.plus(1, TimeUnit.MINUTES).plus(100, TimeUnit.SECONDS), exchanges.get(3).getVersionToInstant());
      //
      assertEquals(now.plus(1, TimeUnit.MINUTES).plus(100, TimeUnit.SECONDS), exchanges.get(2).getVersionFromInstant());
      assertEquals(now.plus(3, TimeUnit.MINUTES), exchanges.get(2).getVersionToInstant());
      //
      assertEquals(now.plus(3, TimeUnit.MINUTES), exchanges.get(1).getVersionFromInstant());
      assertEquals(now.plus(4, TimeUnit.MINUTES), exchanges.get(1).getVersionToInstant());
      //
      assertEquals(now.plus(4, TimeUnit.MINUTES), exchanges.get(0).getVersionFromInstant());
      assertEquals(null, exchanges.get(0).getVersionToInstant());

    } finally {
      _exgMaster.setTimeSource(origTimeSource);
    }
  }


  @Test
  /**
   *
   *       |                                                                 
   *       |                                                                 
   *       |                                                                                                         
   *       |                          
   *       |     setup_4              
   *       |                          
   *   +4m |------------------------ ... ------------------------------------------------>>>        
   *       |                          
   *       |     setup_3                  
   *       |                            
   *   +3m |------------------------ ... ------------------------------------------------>>>      
   *       |                                             
   *       |     setup_2               
   *       |                          
   *       |                           
   *       |                           
   *   +2m |------------------------ ... ------------------------------------------------>>>                                                                   
   *       |                        |             setup_1 (copy)
   *       |     setup_1            |    
   *       |                        |     <-- +2m30s |----------------------------------->>> 
   *   +1m |------------------------|                |      replace_4                                                
   *       |                        |     <-- +2m00s |----------------------------------->>> 
   *       |                        |                |      replace_3                        
   *       |     setup_0            |     <-- +1m30s |----------------------------------->>>       
   *       |                        |                |      replace_2                        
   *   NOW |========================|     <-- +1m00s |----------------------------------->>> 
   *                                                 |      replace_1                        
   *                                      <-- +0m30s |----------------------------------->>> 
   *
   *
   *
   */
  public void test_ReplaceVersions5() {
    TimeSource origTimeSource = _exgMaster.getTimeSource();
    try {
      Instant now = Instant.now();

      ObjectId baseOid = setupTestData(now);
      _exgMaster.setTimeSource(TimeSource.fixed(now.plus(2, TimeUnit.HOURS)));
      ExchangeDocument latestDoc = _exgMaster.get(baseOid, VersionCorrection.LATEST);
      Instant latestFrom = latestDoc.getVersionFromInstant();

      final ExternalIdBundle bundle = ExternalIdBundle.of("B", "B0");
      final ExternalIdBundle region = ExternalIdBundle.of("R", "R0");
      List<ExchangeDocument> replacement = newArrayList();
      for (int i = 1; i <= 4; i++) {
        ManageableExchange ex = new ManageableExchange(bundle, "replace_" + i, region, null);
        ExchangeDocument doc = new ExchangeDocument(ex);
        doc.setVersionFromInstant(now.minus(60, TimeUnit.SECONDS).plus(i * 30, TimeUnit.SECONDS));
        replacement.add(doc);
      }
      replacement.get(replacement.size() - 1).setVersionToInstant(now.plus(90, TimeUnit.SECONDS));

      _exgMaster.replaceVersions(latestDoc, replacement);

      ExchangeHistoryRequest historyRequest = new ExchangeHistoryRequest();
      historyRequest.setObjectId(baseOid);
      historyRequest.setCorrectionsFromInstant(now.plus(2, TimeUnit.HOURS));
      ExchangeHistoryResult result = _exgMaster.history(historyRequest);
      List<ExchangeDocument> exchanges = result.getDocuments();

      assertEquals(8, exchanges.size());

      //
      assertEquals(now.plus(-30, TimeUnit.SECONDS), exchanges.get(7).getVersionFromInstant());
      assertEquals(now.plus(0, TimeUnit.SECONDS), exchanges.get(7).getVersionToInstant());
      //
      assertEquals(now.plus(0, TimeUnit.SECONDS), exchanges.get(6).getVersionFromInstant());
      assertEquals(now.plus(30, TimeUnit.SECONDS), exchanges.get(6).getVersionToInstant());
      //
      assertEquals(now.plus(30, TimeUnit.SECONDS), exchanges.get(5).getVersionFromInstant());
      assertEquals(now.plus(60, TimeUnit.SECONDS), exchanges.get(5).getVersionToInstant());
      //
      assertEquals(now.plus(60, TimeUnit.SECONDS), exchanges.get(4).getVersionFromInstant());
      assertEquals(now.plus(90, TimeUnit.SECONDS), exchanges.get(4).getVersionToInstant());
      //
      assertEquals(now.plus(90, TimeUnit.SECONDS), exchanges.get(3).getVersionFromInstant());
      assertEquals(now.plus(120, TimeUnit.SECONDS), exchanges.get(3).getVersionToInstant());
      //
      assertEquals(now.plus(120, TimeUnit.SECONDS), exchanges.get(2).getVersionFromInstant());
      assertEquals(now.plus(180, TimeUnit.SECONDS), exchanges.get(2).getVersionToInstant());
      //
      assertEquals(now.plus(180, TimeUnit.SECONDS), exchanges.get(1).getVersionFromInstant());
      assertEquals(now.plus(240, TimeUnit.SECONDS), exchanges.get(1).getVersionToInstant());
      //
      assertEquals(now.plus(240, TimeUnit.SECONDS), exchanges.get(0).getVersionFromInstant());
      assertEquals(null, exchanges.get(0).getVersionToInstant());

    } finally {
      _exgMaster.setTimeSource(origTimeSource);
    }
  }
}
