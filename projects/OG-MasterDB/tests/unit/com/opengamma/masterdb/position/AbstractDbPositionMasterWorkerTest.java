/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.masterdb.position;

import static com.opengamma.util.db.DbDateUtils.MAX_SQL_TIMESTAMP;
import static com.opengamma.util.db.DbDateUtils.toSqlDate;
import static com.opengamma.util.db.DbDateUtils.toSqlTimestamp;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.math.BigDecimal;
import java.util.List;
import java.util.TimeZone;

import javax.time.Instant;
import javax.time.TimeSource;
import javax.time.calendar.OffsetDateTime;
import javax.time.calendar.OffsetTime;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.jdbc.core.simple.SimpleJdbcTemplate;

import com.opengamma.id.Identifier;
import com.opengamma.id.IdentifierBundle;
import com.opengamma.id.UniqueIdentifier;
import com.opengamma.master.position.ManageablePosition;
import com.opengamma.master.position.ManageableTrade;
import com.opengamma.master.position.PositionDocument;
import com.opengamma.masterdb.DbMasterTestUtils;
import com.opengamma.util.test.DBTest;

/**
 * Base tests for DbPositionMasterWorker via DbPositionMaster.
 */
@Ignore
public abstract class AbstractDbPositionMasterWorkerTest extends DBTest {

  private static final Logger s_logger = LoggerFactory.getLogger(AbstractDbPositionMasterWorkerTest.class);

  protected DbPositionMaster _posMaster;
  protected Instant _version1Instant;
  protected Instant _version2Instant;
  protected int _totalPortfolios;
  protected int _totalPositions;
  protected OffsetDateTime _now;

  public AbstractDbPositionMasterWorkerTest(String databaseType, String databaseVersion) {
    super(databaseType, databaseVersion);
    s_logger.info("running testcases for {}", databaseType);
    TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
  }

  @Before
  public void setUp() throws Exception {
    super.setUp();
    ConfigurableApplicationContext context = DbMasterTestUtils.getContext(getDatabaseType());
    _posMaster = (DbPositionMaster) context.getBean(getDatabaseType() + "DbPositionMaster");
    
    _now = OffsetDateTime.now();
    _posMaster.setTimeSource(TimeSource.fixed(_now.toInstant()));
    _version1Instant = _now.toInstant().minusSeconds(100);
    _version2Instant = _now.toInstant().minusSeconds(50);
    s_logger.debug("test data now:   {}", _version1Instant);
    s_logger.debug("test data later: {}", _version2Instant);
    final SimpleJdbcTemplate template = _posMaster.getDbSource().getJdbcTemplate();
    template.update("INSERT INTO pos_position VALUES (?,?,?,?,?, ?,?,?,?)",
        100, 100, toSqlTimestamp(_version1Instant), MAX_SQL_TIMESTAMP, toSqlTimestamp(_version1Instant), MAX_SQL_TIMESTAMP, "A", "100", BigDecimal.valueOf(100.987));
    template.update("INSERT INTO pos_position VALUES (?,?,?,?,?, ?,?,?,?)",
        120, 120, toSqlTimestamp(_version1Instant), MAX_SQL_TIMESTAMP, toSqlTimestamp(_version1Instant), MAX_SQL_TIMESTAMP, "A", "120", BigDecimal.valueOf(120.987));
    template.update("INSERT INTO pos_position VALUES (?,?,?,?,?, ?,?,?,?)",
        121, 121, toSqlTimestamp(_version1Instant), MAX_SQL_TIMESTAMP, toSqlTimestamp(_version1Instant), MAX_SQL_TIMESTAMP, "A", "121", BigDecimal.valueOf(121.987));
    template.update("INSERT INTO pos_position VALUES (?,?,?,?,?, ?,?,?,?)",
        122, 122, toSqlTimestamp(_version1Instant), MAX_SQL_TIMESTAMP, toSqlTimestamp(_version1Instant), MAX_SQL_TIMESTAMP, "A", "122", BigDecimal.valueOf(122.987));
    template.update("INSERT INTO pos_position VALUES (?,?,?,?,?, ?,?,?,?)",
        123, 123, toSqlTimestamp(_version1Instant), MAX_SQL_TIMESTAMP, toSqlTimestamp(_version1Instant), MAX_SQL_TIMESTAMP, "A", "123", BigDecimal.valueOf(123.987));
    template.update("INSERT INTO pos_position VALUES (?,?,?,?,?, ?,?,?,?)",
        221, 221, toSqlTimestamp(_version1Instant), toSqlTimestamp(_version2Instant), toSqlTimestamp(_version1Instant), MAX_SQL_TIMESTAMP, "A", "221", BigDecimal.valueOf(221.987));
    template.update("INSERT INTO pos_position VALUES (?,?,?,?,?, ?,?,?,?)",
        222, 221, toSqlTimestamp(_version2Instant), MAX_SQL_TIMESTAMP, toSqlTimestamp(_version2Instant), MAX_SQL_TIMESTAMP, "A", "222", BigDecimal.valueOf(222.987));
    _totalPositions = 6;
    
    template.update("INSERT INTO pos_idkey VALUES (?,?,?)",
        500, "TICKER", "S100");
    template.update("INSERT INTO pos_idkey VALUES (?,?,?)",
        501, "TICKER", "T130");
    template.update("INSERT INTO pos_idkey VALUES (?,?,?)",
        502, "TICKER", "MSFT");
    template.update("INSERT INTO pos_idkey VALUES (?,?,?)",
        503, "NASDAQ", "Micro");
    template.update("INSERT INTO pos_idkey VALUES (?,?,?)",
        504, "TICKER", "ORCL");
    template.update("INSERT INTO pos_idkey VALUES (?,?,?)",
        505, "TICKER", "ORCL134");
    template.update("INSERT INTO pos_idkey VALUES (?,?,?)",
        506, "NASDAQ", "ORCL135");
    template.update("INSERT INTO pos_idkey VALUES (?,?,?)",
        507, "TICKER", "IBMC");
    
    template.update("INSERT INTO pos_position2idkey VALUES (?,?)", 100, 500);
    template.update("INSERT INTO pos_position2idkey VALUES (?,?)", 120, 501);
    template.update("INSERT INTO pos_position2idkey VALUES (?,?)", 121, 502);
    template.update("INSERT INTO pos_position2idkey VALUES (?,?)", 121, 503);
    template.update("INSERT INTO pos_position2idkey VALUES (?,?)", 122, 504);
    template.update("INSERT INTO pos_position2idkey VALUES (?,?)", 123, 505);
    template.update("INSERT INTO pos_position2idkey VALUES (?,?)", 123, 506);
    template.update("INSERT INTO pos_position2idkey VALUES (?,?)", 221, 507);
    template.update("INSERT INTO pos_position2idkey VALUES (?,?)", 222, 507);
    
    OffsetTime tradeTime = _now.toOffsetTime().minusSeconds(400);
    template.update("INSERT INTO pos_trade VALUES(?,?,?,?,?,?,?,?,?,?)", 
        400, 400, 120, 120, BigDecimal.valueOf(120.987), toSqlDate(_now.toLocalDate()), toSqlTimestamp(tradeTime), tradeTime.getOffset().getAmountSeconds(), "CPARTY", "C100");
    tradeTime = _now.toOffsetTime().minusSeconds(401);
    template.update("INSERT INTO pos_trade VALUES(?,?,?,?,?,?,?,?,?,?)", 
        401, 401, 121, 121, BigDecimal.valueOf(121.987), toSqlDate(_now.toLocalDate()), toSqlTimestamp(tradeTime), tradeTime.getOffset().getAmountSeconds(), "CPARTY", "C101");
    tradeTime = _now.toOffsetTime().minusSeconds(402);
    template.update("INSERT INTO pos_trade VALUES(?,?,?,?,?,?,?,?,?,?)", 
        402, 402, 122, 122, BigDecimal.valueOf(100.987), toSqlDate(_now.toLocalDate()), toSqlTimestamp(tradeTime), tradeTime.getOffset().getAmountSeconds(), "CPARTY", "JMP");
    tradeTime = _now.toOffsetTime().minusSeconds(403);
    template.update("INSERT INTO pos_trade VALUES(?,?,?,?,?,?,?,?,?,?)", 
        403, 403, 122, 122, BigDecimal.valueOf(22.987), toSqlDate(_now.toLocalDate()), toSqlTimestamp(tradeTime), tradeTime.getOffset().getAmountSeconds(), "CPARTY", "CISC");
    tradeTime = _now.toOffsetTime().minusSeconds(404);
    template.update("INSERT INTO pos_trade VALUES(?,?,?,?,?,?,?,?,?,?)", 
        404, 404, 123, 123, BigDecimal.valueOf(100.987), toSqlDate(_now.toLocalDate()), toSqlTimestamp(tradeTime), tradeTime.getOffset().getAmountSeconds(), "CPARTY", "C104");
    tradeTime = _now.toOffsetTime().minusSeconds(405);
    template.update("INSERT INTO pos_trade VALUES(?,?,?,?,?,?,?,?,?,?)", 
        405, 405, 123, 123, BigDecimal.valueOf(200.987), toSqlDate(_now.toLocalDate()), toSqlTimestamp(tradeTime), tradeTime.getOffset().getAmountSeconds(), "CPARTY", "C105");
    tradeTime = _now.toOffsetTime().minusSeconds(406);
    template.update("INSERT INTO pos_trade VALUES(?,?,?,?,?,?,?,?,?,?)", 
        406, 406, 123, 123, BigDecimal.valueOf(300.987), toSqlDate(_now.toLocalDate()), toSqlTimestamp(tradeTime), tradeTime.getOffset().getAmountSeconds(), "CPARTY", "C106");
    tradeTime = _now.toOffsetTime().minusSeconds(407);
    template.update("INSERT INTO pos_trade VALUES(?,?,?,?,?,?,?,?,?,?)", 
        407, 407, 221, 221, BigDecimal.valueOf(221.987), toSqlDate(_now.toLocalDate()), toSqlTimestamp(tradeTime), tradeTime.getOffset().getAmountSeconds(), "CPARTY", "C221");
    tradeTime = _now.toOffsetTime().minusSeconds(408);
    template.update("INSERT INTO pos_trade VALUES(?,?,?,?,?,?,?,?,?,?)", 
        408, 407, 222, 221, BigDecimal.valueOf(222.987), toSqlDate(_now.toLocalDate()), toSqlTimestamp(tradeTime), tradeTime.getOffset().getAmountSeconds(), "CPARTY", "C222");
    
    template.update("INSERT INTO pos_trade2idkey VALUES (?,?)", 400, 501);
    template.update("INSERT INTO pos_trade2idkey VALUES (?,?)", 401, 502);
    template.update("INSERT INTO pos_trade2idkey VALUES (?,?)", 401, 503);
    template.update("INSERT INTO pos_trade2idkey VALUES (?,?)", 402, 504);
    template.update("INSERT INTO pos_trade2idkey VALUES (?,?)", 403, 504);
    template.update("INSERT INTO pos_trade2idkey VALUES (?,?)", 404, 505);
    template.update("INSERT INTO pos_trade2idkey VALUES (?,?)", 404, 506);
    template.update("INSERT INTO pos_trade2idkey VALUES (?,?)", 405, 505);
    template.update("INSERT INTO pos_trade2idkey VALUES (?,?)", 405, 506);
    template.update("INSERT INTO pos_trade2idkey VALUES (?,?)", 406, 505);
    template.update("INSERT INTO pos_trade2idkey VALUES (?,?)", 406, 506);
    template.update("INSERT INTO pos_trade2idkey VALUES (?,?)", 407, 507);
    template.update("INSERT INTO pos_trade2idkey VALUES (?,?)", 408, 507);
  }

  @After
  public void tearDown() throws Exception {
    _posMaster = null;
    super.tearDown();
  }

  //-------------------------------------------------------------------------
  protected void assert100(final PositionDocument test) {
    UniqueIdentifier uid = UniqueIdentifier.of("DbPos", "100", "0");
    assertNotNull(test);
    assertEquals(uid, test.getUniqueId());
    assertEquals(_version1Instant, test.getVersionFromInstant());
    assertEquals(null, test.getVersionToInstant());
    assertEquals(_version1Instant, test.getCorrectionFromInstant());
    assertEquals(null, test.getCorrectionToInstant());
    assertEquals(Identifier.of("A", "100"), test.getProviderKey());
    ManageablePosition position = test.getPosition();
    assertNotNull(position);
    assertEquals(uid, position.getUniqueId());
    assertEquals(BigDecimal.valueOf(100.987), position.getQuantity());
    IdentifierBundle secKey = position.getSecurityKey();
    assertNotNull(secKey);
    assertEquals(1, secKey.size());
    assertEquals(true, secKey.getIdentifiers().contains(Identifier.of("TICKER", "S100")));
    
    List<ManageableTrade> trades = position.getTrades();
    assertNotNull(trades);
    assertTrue(trades.isEmpty());
  }

  protected void assert120(final PositionDocument test) {
    UniqueIdentifier uid = UniqueIdentifier.of("DbPos", "120", "0");
    assertNotNull(test);
    assertEquals(uid, test.getUniqueId());
    assertEquals(_version1Instant, test.getVersionFromInstant());
    assertEquals(null, test.getVersionToInstant());
    assertEquals(_version1Instant, test.getCorrectionFromInstant());
    assertEquals(null, test.getCorrectionToInstant());
    assertEquals(Identifier.of("A", "120"), test.getProviderKey());
    ManageablePosition position = test.getPosition();
    assertNotNull(position);
    assertEquals(uid, position.getUniqueId());
    assertEquals(BigDecimal.valueOf(120.987), position.getQuantity());
    IdentifierBundle secKey = position.getSecurityKey();
    assertNotNull(secKey);
    assertEquals(1, secKey.size());
    assertEquals(true, secKey.getIdentifiers().contains(Identifier.of("TICKER", "T130")));
    List<ManageableTrade> trades = position.getTrades();
    assertEquals(1, trades.size());
    ManageableTrade trade = trades.get(0);
    assertNotNull(trade);
    assertEquals(UniqueIdentifier.of("DbPos", "400", "0"), trade.getUniqueId());
    assertEquals(uid, trade.getPositionId());
    assertEquals(Identifier.of("CPARTY", "C100"), trade.getCounterpartyKey());
    assertEquals(BigDecimal.valueOf(120.987), trade.getQuantity());
    assertEquals(_now.toLocalDate(), trade.getTradeDate());
    assertEquals(_now.toOffsetTime().minusSeconds(400), trade.getTradeTime());
    assertEquals(true, trade.getSecurityKey().getIdentifiers().contains(Identifier.of("TICKER", "T130")));
  }

  protected void assert121(final PositionDocument test) {
    UniqueIdentifier uid = UniqueIdentifier.of("DbPos", "121", "0");
    assertNotNull(test);
    assertEquals(uid, test.getUniqueId());
    assertEquals(_version1Instant, test.getVersionFromInstant());
    assertEquals(null, test.getVersionToInstant());
    assertEquals(_version1Instant, test.getCorrectionFromInstant());
    assertEquals(null, test.getCorrectionToInstant());
    assertEquals(Identifier.of("A", "121"), test.getProviderKey());
    ManageablePosition position = test.getPosition();
    assertNotNull(position);
    assertEquals(uid, position.getUniqueId());
    assertEquals(BigDecimal.valueOf(121.987), position.getQuantity());
    IdentifierBundle secKey = position.getSecurityKey();
    assertNotNull(secKey);
    assertEquals(2, secKey.size());
    assertEquals(true, secKey.getIdentifiers().contains(Identifier.of("TICKER", "MSFT")));
    assertEquals(true, secKey.getIdentifiers().contains(Identifier.of("NASDAQ", "Micro")));
    List<ManageableTrade> trades = position.getTrades();
    assertEquals(1, trades.size());
    ManageableTrade trade = trades.get(0);
    assertNotNull(trade);
    assertEquals(UniqueIdentifier.of("DbPos", "401", "0"), trade.getUniqueId());
    assertEquals(uid, trade.getPositionId());
    assertEquals(Identifier.of("CPARTY", "C101"), trade.getCounterpartyKey());
    assertEquals(BigDecimal.valueOf(121.987), trade.getQuantity());
    assertEquals(_now.toLocalDate(), trade.getTradeDate());
    assertEquals(_now.toOffsetTime().minusSeconds(401), trade.getTradeTime());
  }

  protected void assert122(final PositionDocument test) {
    UniqueIdentifier uid = UniqueIdentifier.of("DbPos", "122", "0");
    assertNotNull(test);
    assertEquals(uid, test.getUniqueId());
    assertEquals(_version1Instant, test.getVersionFromInstant());
    assertEquals(null, test.getVersionToInstant());
    assertEquals(_version1Instant, test.getCorrectionFromInstant());
    assertEquals(null, test.getCorrectionToInstant());
    assertEquals(Identifier.of("A", "122"), test.getProviderKey());
    ManageablePosition position = test.getPosition();
    assertNotNull(position);
    assertEquals(uid, position.getUniqueId());
    assertEquals(BigDecimal.valueOf(122.987), position.getQuantity());
    IdentifierBundle secKey = position.getSecurityKey();
    assertNotNull(secKey);
    assertEquals(1, secKey.size());
    assertEquals(Identifier.of("TICKER", "ORCL"), secKey.getIdentifiers().iterator().next());
    assertEquals(2, position.getTrades().size());
  }

  protected void assert123(final PositionDocument test) {
    UniqueIdentifier uid = UniqueIdentifier.of("DbPos", "123", "0");
    assertNotNull(test);
    assertEquals(uid, test.getUniqueId());
    assertEquals(_version1Instant, test.getVersionFromInstant());
    assertEquals(null, test.getVersionToInstant());
    assertEquals(_version1Instant, test.getCorrectionFromInstant());
    assertEquals(null, test.getCorrectionToInstant());
    assertEquals(Identifier.of("A", "123"), test.getProviderKey());
    ManageablePosition position = test.getPosition();
    assertNotNull(position);
    assertEquals(uid, position.getUniqueId());
    assertEquals(BigDecimal.valueOf(123.987), position.getQuantity());
    IdentifierBundle secKey = position.getSecurityKey();
    assertNotNull(secKey);
    assertEquals(2, secKey.size());
    assertTrue(secKey.getIdentifiers().contains(Identifier.of("NASDAQ", "ORCL135")));
    assertTrue(secKey.getIdentifiers().contains(Identifier.of("TICKER", "ORCL134")));
    List<ManageableTrade> trades = position.getTrades();
    assertEquals(3, trades.size());
    
    ManageableTrade trade = new ManageableTrade(BigDecimal.valueOf(100.987), secKey, _now.toLocalDate(), _now.toOffsetTime().minusSeconds(404), Identifier.of("CPARTY", "C104"));
    trade.setPositionId(uid);
    trade.setUniqueId(UniqueIdentifier.of("DbPos", "404", "0"));
    assertTrue(trades.contains(trade));
    
    trade = new ManageableTrade(BigDecimal.valueOf(200.987), secKey, _now.toLocalDate(), _now.toOffsetTime().minusSeconds(405), Identifier.of("CPARTY", "C105"));
    trade.setPositionId(uid);
    trade.setUniqueId(UniqueIdentifier.of("DbPos", "405", "0"));
    assertTrue(trades.contains(trade));
    
    trade = new ManageableTrade(BigDecimal.valueOf(300.987), secKey, _now.toLocalDate(), _now.toOffsetTime().minusSeconds(406),Identifier.of("CPARTY", "C106"));
    trade.setPositionId(uid);
    trade.setUniqueId(UniqueIdentifier.of("DbPos", "406", "0"));
    assertTrue(trades.contains(trade));
  }

  protected void assert221(final PositionDocument test) {
    UniqueIdentifier uid = UniqueIdentifier.of("DbPos", "221", "0");
    assertNotNull(test);
    assertEquals(uid, test.getUniqueId());
    assertEquals(_version1Instant, test.getVersionFromInstant());
    assertEquals(_version2Instant, test.getVersionToInstant());
    assertEquals(_version1Instant, test.getCorrectionFromInstant());
    assertEquals(null, test.getCorrectionToInstant());
    assertEquals(Identifier.of("A", "221"), test.getProviderKey());
    ManageablePosition position = test.getPosition();
    assertNotNull(position);
    assertEquals(uid, position.getUniqueId());
    assertEquals(BigDecimal.valueOf(221.987), position.getQuantity());
    IdentifierBundle secKey = position.getSecurityKey();
    assertNotNull(secKey);
    assertEquals(1, secKey.size());
    assertEquals(Identifier.of("TICKER", "IBMC"), secKey.getIdentifiers().iterator().next());
    List<ManageableTrade> trades = position.getTrades();
    assertEquals(1, trades.size());
    ManageableTrade expected = new ManageableTrade(BigDecimal.valueOf(221.987), secKey, _now.toLocalDate(), _now.toOffsetTime().minusSeconds(407), Identifier.of("CPARTY", "C221"));
    expected.setPositionId(uid);
    expected.setUniqueId(UniqueIdentifier.of("DbPos", "407", "0"));
    assertTrue(trades.contains(expected));
  }

  protected void assert222(final PositionDocument test) {
    UniqueIdentifier uid = UniqueIdentifier.of("DbPos", "221", "1");
    assertNotNull(test);
    assertEquals(uid, test.getUniqueId());
    assertEquals(_version2Instant, test.getVersionFromInstant());
    assertEquals(null, test.getVersionToInstant());
    assertEquals(_version2Instant, test.getCorrectionFromInstant());
    assertEquals(null, test.getCorrectionToInstant());
    assertEquals(Identifier.of("A", "222"), test.getProviderKey());
    ManageablePosition position = test.getPosition();
    assertNotNull(position);
    assertEquals(uid, position.getUniqueId());
    assertEquals(BigDecimal.valueOf(222.987), position.getQuantity());
    IdentifierBundle secKey = position.getSecurityKey();
    assertNotNull(secKey);
    assertEquals(1, secKey.size());
    assertEquals(Identifier.of("TICKER", "IBMC"), secKey.getIdentifiers().iterator().next());
    List<ManageableTrade> trades = position.getTrades();
    assertEquals(1, trades.size());
    ManageableTrade expected = new ManageableTrade(BigDecimal.valueOf(222.987), secKey, _now.toLocalDate(), _now.toOffsetTime().minusSeconds(408), Identifier.of("CPARTY", "C222"));
    expected.setPositionId(uid);
    expected.setUniqueId(UniqueIdentifier.of("DbPos", "407", "1"));
    assertTrue(trades.contains(expected));
  }

}
