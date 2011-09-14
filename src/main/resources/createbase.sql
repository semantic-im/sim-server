# --------------------------------------------------------
# Host:                         localhost
# Server version:               5.0.24a-community-nt
# Server OS:                    Win32
# HeidiSQL version:             6.0.0.3603
# Date/time:                    2011-09-13 17:46:16
# --------------------------------------------------------

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET NAMES utf8 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;

# Dumping database structure for larkc
CREATE DATABASE IF NOT EXISTS `larkc` /*!40100 DEFAULT CHARACTER SET latin1 */;
USE `larkc`;


# Dumping structure for table larkc.metrics
CREATE TABLE IF NOT EXISTS `metrics` (
  `idMetric` int(10) unsigned NOT NULL auto_increment,
  `Name` varchar(50) default NULL,
  `MetricType` int(10) unsigned NOT NULL,
  `Title` varchar(50) default NULL,
  `Description` varchar(50) default NULL,
  `MetricTableName` varchar(50) default NULL,
  PRIMARY KEY  (`idMetric`),
  UNIQUE KEY `idMetric_UNIQUE` (`idMetric`,`Name`),
  KEY `FK_metrics_mtypes` (`MetricType`),
  CONSTRAINT `FK_metrics_mtypes` FOREIGN KEY (`MetricType`) REFERENCES `mtypes` (`idType`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1 COMMENT='\n';

# Dumping data for table larkc.metrics: ~145 rows (approximately)
/*!40000 ALTER TABLE `metrics` DISABLE KEYS */;
INSERT IGNORE INTO `metrics` (`idMetric`, `Name`, `MetricType`, `Title`, `Description`, `MetricTableName`) VALUES
	(1, 'QueryBeginExecutionTime', 2, NULL, NULL, NULL),
	(2, 'QueryEndExecutionTime', 2, NULL, NULL, NULL),
	(3, 'QueryErrorStatus', 3, NULL, NULL, NULL),
	(5, 'QuerySizeInCharacters', 2, NULL, NULL, NULL),
	(6, 'QueryNamespaceNb', 2, NULL, NULL, NULL),
	(7, 'QueryVariablesNb', 2, NULL, NULL, NULL),
	(8, 'QueryDataSetSourcesNb', 2, NULL, NULL, NULL),
	(9, 'QueryOperatorsNb', 2, NULL, NULL, NULL),
	(10, 'QueryResultOrderingNb', 2, NULL, NULL, NULL),
	(11, 'QueryResultLimitNb', 2, NULL, NULL, NULL),
	(12, 'QueryResultOffsetNb', 2, NULL, NULL, NULL),
	(13, 'QueryResultSizeInCharacters', 2, NULL, NULL, NULL),
	(14, 'QueryTotalResponseTime', 2, NULL, NULL, NULL),
	(15, 'QueryProcessTotalCPUTime', 2, NULL, NULL, NULL),
	(16, 'QueryThreadTotalCPUTime', 2, NULL, NULL, NULL),
	(17, 'QueryThreadUserCPUTime', 2, NULL, NULL, NULL),
	(18, 'QueryThreadSystemCPUTime', 2, NULL, NULL, NULL),
	(19, 'QueryThreadCount', 2, NULL, NULL, NULL),
	(20, 'QueryThreadBlockCount', 2, NULL, NULL, NULL),
	(21, 'QueryThreadBlockTime', 2, NULL, NULL, NULL),
	(22, 'QueryThreadWaitCount', 2, NULL, NULL, NULL),
	(23, 'QueryThreadWaitTime', 2, NULL, NULL, NULL),
	(24, 'QueryThreadGccCount', 2, NULL, NULL, NULL),
	(25, 'QueryThreadGccTime', 2, NULL, NULL, NULL),
	(26, 'WorkflowNumberOfPlugins', 2, NULL, NULL, NULL),
	(27, 'WorkflowTotalResponseTime', 2, NULL, NULL, NULL),
	(28, 'WorkflowProcessTotalCPUTime', 2, NULL, NULL, NULL),
	(29, 'WorkflowThreadTotalCPUTime', 2, NULL, NULL, NULL),
	(30, 'WorkflowThreadUserCPUTime', 2, NULL, NULL, NULL),
	(31, 'WorkflowThreadSystemCPUTime', 2, NULL, NULL, NULL),
	(32, 'WorkflowThreadCount', 2, NULL, NULL, NULL),
	(33, 'WorkflowThreadBlockCount', 2, NULL, NULL, NULL),
	(34, 'WorkflowThreadBlockTime', 2, NULL, NULL, NULL),
	(35, 'WorkflowThreadWaitCount', 2, NULL, NULL, NULL),
	(36, 'WorkflowThreadWaitTime', 2, NULL, NULL, NULL),
	(37, 'WorkflowThreadGccCount', 2, NULL, NULL, NULL),
	(38, 'WorkflowThreadGccTime', 2, NULL, NULL, NULL),
	(39, 'PluginBeginExecutionTime', 2, NULL, NULL, NULL),
	(40, 'PluginEndExecutionTime', 2, NULL, NULL, NULL),
	(41, 'PluginErrorStatus', 3, NULL, NULL, NULL),
	(42, 'PluginTotalResponseTime', 2, NULL, NULL, NULL),
	(43, 'PluginProcessTotalCPUTime', 2, NULL, NULL, NULL),
	(44, 'PluginThreadTotalCPUTime', 2, NULL, NULL, NULL),
	(45, 'PluginThreadUserCPUTime', 2, NULL, NULL, NULL),
	(46, 'PluginThreadSystemCPUTime', 2, NULL, NULL, NULL),
	(47, 'PluginThreadCount', 2, NULL, NULL, NULL),
	(48, 'PluginThreadBlockCount', 2, NULL, NULL, NULL),
	(49, 'PluginThreadBlockTime', 2, NULL, NULL, NULL),
	(50, 'PluginThreadWaitCount', 2, NULL, NULL, NULL),
	(51, 'PluginThreadWaitTime', 2, NULL, NULL, NULL),
	(52, 'PluginThreadGccCount', 2, NULL, NULL, NULL),
	(53, 'PluginThreadGccTime', 2, NULL, NULL, NULL),
	(54, 'PluginInputSizeInTriples', 2, NULL, NULL, NULL),
	(55, 'PluginOutputSizeInTriples', 2, NULL, NULL, NULL),
	(56, 'PluginCacheHit', 2, NULL, NULL, NULL),
	(57, 'PlatformCPUUsage', 2, NULL, NULL, NULL),
	(58, 'PlatformAvgCPUUsage', 2, NULL, NULL, NULL),
	(59, 'PlatformCPUTime', 2, NULL, NULL, NULL),
	(60, 'PlatformGccCount', 2, NULL, NULL, NULL),
	(61, 'PlatformGccTime', 2, NULL, NULL, NULL),
	(62, 'PlatformUsedMemory', 2, NULL, NULL, NULL),
	(63, 'PlatformUptime', 2, NULL, NULL, NULL),
	(64, 'QueryNumberOfExceptions', 2, NULL, NULL, NULL),
	(65, 'WorkflowNumberOfExceptions', 2, NULL, NULL, NULL),
	(66, 'PluginNumberOfExceptions', 2, NULL, NULL, NULL),
	(67, 'QueryDataLayerInserts', 2, NULL, NULL, NULL),
	(68, 'WorkflowDataLayerInserts', 2, NULL, NULL, NULL),
	(69, 'PluginDataLayerInserts', 2, NULL, NULL, NULL),
	(70, 'QueryDataLayerSelects', 2, NULL, NULL, NULL),
	(71, 'WorkflowDataLayerSelects', 2, NULL, NULL, NULL),
	(72, 'PluginDataLayerSelects', 2, NULL, NULL, NULL),
	(73, 'QueryNumberOfOutOfMemoryExceptions', 2, NULL, NULL, NULL),
	(74, 'WorkflowNumberOfOutOfMemoryExceptions', 2, NULL, NULL, NULL),
	(76, 'PluginNumberOfOutOfMemoryExceptions', 2, NULL, NULL, NULL),
	(77, 'QueryNumberOfMalformedSparqlQueryExceptions', 2, NULL, NULL, NULL),
	(78, 'WorkflowNumberOfMalformedSparqlQueryException', 2, NULL, NULL, NULL),
	(79, 'PluginNumberOfMalformedSparqlQueryExceptions', 2, NULL, NULL, NULL),
	(80, 'QueryErrorMessage', 1, NULL, NULL, NULL),
	(81, 'QueryAllocatedMemoryBefore', 2, NULL, NULL, NULL),
	(82, 'QueryAllocatedMemoryAfter', 2, NULL, NULL, NULL),
	(83, 'QueryUsedMemoryBefore', 2, NULL, NULL, NULL),
	(84, 'QueryUsedMemoryAfter', 2, NULL, NULL, NULL),
	(85, 'QueryFreeMemoryBefore', 2, NULL, NULL, NULL),
	(86, 'QueryFreeMemoryAfter', 2, NULL, NULL, NULL),
	(87, 'QueryUnallocatedMemoryBefore', 2, NULL, NULL, NULL),
	(88, 'QueryUnallocatedMemoryAfter', 2, NULL, NULL, NULL),
	(89, 'PlatformThreadsCount', 2, NULL, NULL, NULL),
	(90, 'PlatformThreadsStarted', 2, NULL, NULL, NULL),
	(91, 'PlatformTotalThreadsStarted', 2, NULL, NULL, NULL),
	(92, 'WorkflowErrorMessage', 1, NULL, NULL, NULL),
	(94, 'WorkflowAllocatedMemoryBefore', 2, NULL, NULL, NULL),
	(95, 'WorkflowAllocatedMemoryAfter', 2, NULL, NULL, NULL),
	(96, 'WorkflowUsedMemoryBefore', 2, NULL, NULL, NULL),
	(97, 'WorkflowUsedMemoryAfter', 2, NULL, NULL, NULL),
	(98, 'WorkflowFreeMemoryBefore', 2, NULL, NULL, NULL),
	(99, 'WorkflowFreeMemoryAfter', 2, NULL, NULL, NULL),
	(100, 'WorkflowUnallocatedMemoryBefore', 2, NULL, NULL, NULL),
	(101, 'WorkflowUnallocatedMemoryAfter', 2, ' ', NULL, NULL),
	(103, 'PluginErrorMessage', 1, '', '', ''),
	(104, 'PluginAllocatedMemoryBefore', 2, '', '', ''),
	(105, 'PluginAllocatedMemoryAfter', 2, '', '', ''),
	(106, 'PluginUsedMemoryBefore', 2, '', '', ''),
	(107, 'PluginUsedMemoryAfter', 2, '', '', ''),
	(108, 'PluginFreeMemoryBefore', 2, '', '', ''),
	(109, 'PluginFreeMemoryAfter', 2, '', '', ''),
	(110, 'PluginUnallocatedMemoryBefore', 2, '', '', ''),
	(111, 'PluginUnallocatedMemoryAfter', 2, '', '', ''),
	(112, 'PlatformTotalCPUTime', 2, ' ', ' ', ' '),
	(116, 'PlatformTotalGccCount', 2, ' ', ' ', ' '),
	(117, 'PlatformTotalGccTime', 2, ' ', ' ', ' '),
	(119, 'PlatformAllocatedMemory', 2, ' ', ' ', ' '),
	(121, 'PlatformFreeMemory', 2, ' ', ' ', ' '),
	(122, 'PlatformUnallocatedMemory', 2, ' ', ' ', ' '),
	(123, 'QueryNamespaceValues', 1, NULL, NULL, NULL),
	(124, 'QueryDataSetSources', 1, NULL, NULL, NULL),
	(125, 'QueryLiteralsNb', 2, NULL, NULL, NULL),
	(126, 'SystemLoadAverage', 2, NULL, NULL, NULL),
	(127, 'SystemTotalFreeMemory', 2, NULL, NULL, NULL),
	(128, 'SystemTotalUsedMemory', 2, NULL, NULL, NULL),
	(129, 'SystemTotalUsedSwap', 2, NULL, NULL, NULL),
	(130, 'SystemOpenFileDescrCnt', 2, NULL, NULL, NULL),
	(131, 'SystemSwapIn', 2, NULL, NULL, NULL),
	(132, 'SystemSwapOut', 2, NULL, NULL, NULL),
	(133, 'SystemIORead', 2, NULL, NULL, NULL),
	(134, 'SystemIOWrite', 2, NULL, NULL, NULL),
	(135, 'SystemUserCPULoad', 2, NULL, NULL, NULL),
	(136, 'SystemCPULoad', 2, NULL, NULL, NULL),
	(137, 'SystemIdleCPULoad', 2, NULL, NULL, NULL),
	(138, 'SystemWaitCPULoad', 2, NULL, NULL, NULL),
	(139, 'SystemIrqCPULoad', 2, NULL, NULL, NULL),
	(140, 'SystemUserCPUTime', 2, NULL, NULL, NULL),
	(141, 'SystemCPUTime', 2, NULL, NULL, NULL),
	(142, 'SystemIdleCPUTime', 2, NULL, NULL, NULL),
	(143, 'SystemWaitCPUTime', 2, NULL, NULL, NULL),
	(144, 'SystemIrqCPUTime', 2, NULL, NULL, NULL),
	(145, 'SystemProcessesCount', 2, NULL, NULL, NULL),
	(146, 'SystemThreadsCount', 2, NULL, NULL, NULL),
	(147, 'SystemRunningProcessesCount', 2, NULL, NULL, NULL),
	(148, 'SystemRunningThreadsCount', 2, NULL, NULL, NULL),
	(149, 'SystemTcpOutbound', 2, NULL, NULL, NULL),
	(150, 'SystemTcpInbound', 2, NULL, NULL, NULL),
	(151, 'SystemNetworkSent', 2, NULL, NULL, NULL),
	(152, 'SystemNetworkReceived', 2, NULL, NULL, NULL),
	(153, 'SystemLoopbackNetworkSent', 2, NULL, NULL, NULL),
	(154, 'SystemLoopbackNetworkReceived', 2, NULL, NULL, NULL);
/*!40000 ALTER TABLE `metrics` ENABLE KEYS */;


# Dumping structure for table larkc.metrics_mtypes
CREATE TABLE IF NOT EXISTS `metrics_mtypes` (
  `idMetrics_MTypes` int(10) unsigned NOT NULL auto_increment,
  `fk_idMetric` varchar(45) NOT NULL,
  `fk_idType` varchar(45) NOT NULL,
  PRIMARY KEY  (`idMetrics_MTypes`,`fk_idMetric`,`fk_idType`),
  UNIQUE KEY `idMetrics_MTypes_UNIQUE` (`idMetrics_MTypes`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

# Dumping data for table larkc.metrics_mtypes: ~0 rows (approximately)
/*!40000 ALTER TABLE `metrics_mtypes` DISABLE KEYS */;
/*!40000 ALTER TABLE `metrics_mtypes` ENABLE KEYS */;


# Dumping structure for table larkc.mtypes
CREATE TABLE IF NOT EXISTS `mtypes` (
  `idType` int(10) unsigned NOT NULL auto_increment,
  `Name` varchar(45) default NULL,
  PRIMARY KEY  (`idType`),
  UNIQUE KEY `idMType` (`idType`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

# Dumping data for table larkc.mtypes: ~4 rows (approximately)
/*!40000 ALTER TABLE `mtypes` DISABLE KEYS */;
INSERT IGNORE INTO `mtypes` (`idType`, `Name`) VALUES
	(1, 'STRING'),
	(2, 'NUMERIC'),
	(3, 'CLASS'),
	(4, 'TIMESTAMP');
/*!40000 ALTER TABLE `mtypes` ENABLE KEYS */;


# Dumping structure for table larkc.platforms
CREATE TABLE IF NOT EXISTS `platforms` (
  `idPlatform` varchar(45) NOT NULL,
  `PlatformName` varchar(100) default NULL,
  `ApplicationName` varchar(45) default NULL,
  PRIMARY KEY  (`idPlatform`),
  UNIQUE KEY `idPlatform_UNIQUE` (`idPlatform`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

# Dumping data for table larkc.platforms: ~1 rows (approximately)
/*!40000 ALTER TABLE `platforms` DISABLE KEYS */;
/*!40000 ALTER TABLE `platforms` ENABLE KEYS */;


# Dumping structure for table larkc.platforms_metrics
CREATE TABLE IF NOT EXISTS `platforms_metrics` (
  `Platforms_idPlatform` varchar(45) NOT NULL,
  `Metrics_idMetric` int(10) unsigned NOT NULL,
  `Value` varchar(45) default NULL,
  `Timestamp` timestamp NOT NULL default '0000-00-00 00:00:00',
  PRIMARY KEY  (`Platforms_idPlatform`,`Metrics_idMetric`,`Timestamp`),
  KEY `FK_platforms_metrics_metrics` (`Metrics_idMetric`),
  CONSTRAINT `FK_platforms_metrics_metrics` FOREIGN KEY (`Metrics_idMetric`) REFERENCES `metrics` (`idMetric`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

# Dumping data for table larkc.platforms_metrics: ~573 rows (approximately)
/*!40000 ALTER TABLE `platforms_metrics` DISABLE KEYS */;
/*!40000 ALTER TABLE `platforms_metrics` ENABLE KEYS */;


# Dumping structure for table larkc.platforms_metrics_aggregated
CREATE TABLE IF NOT EXISTS `platforms_metrics_aggregated` (
  `Platforms_idPlatform` varchar(45) NOT NULL,
  `Metrics_idMetric` int(10) unsigned NOT NULL,
  `Value` varchar(45) default NULL,
  `Level` tinyint(4) default NULL,
  `noAggregatedValues` bigint(20) default NULL,
  `startTimestamp` timestamp NULL default NULL,
  `endTimestamp` timestamp NULL default NULL,
  PRIMARY KEY  (`Platforms_idPlatform`,`Metrics_idMetric`),
  KEY `FK_platforms_metrics_aggregated_metrics` (`Metrics_idMetric`),
  CONSTRAINT `FK_platforms_metrics_aggregated_metrics` FOREIGN KEY (`Metrics_idMetric`) REFERENCES `metrics` (`idMetric`),
  CONSTRAINT `FK_platforms_metrics_aggregated_platforms_metrics` FOREIGN KEY (`Platforms_idPlatform`) REFERENCES `platforms` (`idPlatform`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

# Dumping data for table larkc.platforms_metrics_aggregated: ~0 rows (approximately)
/*!40000 ALTER TABLE `platforms_metrics_aggregated` DISABLE KEYS */;
/*!40000 ALTER TABLE `platforms_metrics_aggregated` ENABLE KEYS */;


# Dumping structure for table larkc.platforms_workflows
CREATE TABLE IF NOT EXISTS `platforms_workflows` (
  `Platforms_idPlatform` varchar(45) NOT NULL,
  `Workflows_idWorkflow` varchar(45) NOT NULL,
  PRIMARY KEY  (`Workflows_idWorkflow`,`Platforms_idPlatform`),
  KEY `FK_platforms_workflows_platforms` (`Platforms_idPlatform`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

# Dumping data for table larkc.platforms_workflows: ~2 rows (approximately)
/*!40000 ALTER TABLE `platforms_workflows` DISABLE KEYS */;
/*!40000 ALTER TABLE `platforms_workflows` ENABLE KEYS */;


# Dumping structure for table larkc.plugins
CREATE TABLE IF NOT EXISTS `plugins` (
  `idPlugin` varchar(45) NOT NULL,
  `Name` varchar(256) default NULL,
  PRIMARY KEY  (`idPlugin`),
  UNIQUE KEY `idPlugin_UNIQUE` (`idPlugin`),
  UNIQUE KEY `Name` (`Name`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

# Dumping data for table larkc.plugins: ~2 rows (approximately)
/*!40000 ALTER TABLE `plugins` DISABLE KEYS */;
/*!40000 ALTER TABLE `plugins` ENABLE KEYS */;


# Dumping structure for table larkc.plugins_metrics
CREATE TABLE IF NOT EXISTS `plugins_metrics` (
  `Plugins_idPlugin` varchar(45) NOT NULL,
  `Metrics_idMetric` int(10) unsigned NOT NULL,
  `Value` varchar(45) default NULL,
  `Timestamp` timestamp NOT NULL default '0000-00-00 00:00:00',
  PRIMARY KEY  (`Plugins_idPlugin`,`Metrics_idMetric`,`Timestamp`),
  KEY `FK_plugins_metrics_metrics` (`Metrics_idMetric`),
  CONSTRAINT `FK_plugins_metrics_metrics` FOREIGN KEY (`Metrics_idMetric`) REFERENCES `metrics` (`idMetric`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

# Dumping data for table larkc.plugins_metrics: ~53 rows (approximately)
/*!40000 ALTER TABLE `plugins_metrics` DISABLE KEYS */;
/*!40000 ALTER TABLE `plugins_metrics` ENABLE KEYS */;


# Dumping structure for table larkc.plugins_metrics_aggregated
CREATE TABLE IF NOT EXISTS `plugins_metrics_aggregated` (
  `Plugins_idPlugin` int(10) NOT NULL,
  `Metrics_idMetric` int(10) unsigned NOT NULL,
  `Value` varchar(45) default NULL,
  `Level` tinyint(4) default NULL,
  PRIMARY KEY  (`Plugins_idPlugin`,`Metrics_idMetric`),
  KEY `FK_plugins_metrics_aggregated_metrics` (`Metrics_idMetric`),
  CONSTRAINT `FK_plugins_metrics_aggregated_metrics` FOREIGN KEY (`Metrics_idMetric`) REFERENCES `metrics` (`idMetric`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

# Dumping data for table larkc.plugins_metrics_aggregated: ~0 rows (approximately)
/*!40000 ALTER TABLE `plugins_metrics_aggregated` DISABLE KEYS */;
/*!40000 ALTER TABLE `plugins_metrics_aggregated` ENABLE KEYS */;


# Dumping structure for table larkc.properties
CREATE TABLE IF NOT EXISTS `properties` (
  `idProperty` int(10) unsigned NOT NULL auto_increment,
  PRIMARY KEY  (`idProperty`),
  UNIQUE KEY `idProperty_UNIQUE` (`idProperty`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

# Dumping data for table larkc.properties: ~0 rows (approximately)
/*!40000 ALTER TABLE `properties` DISABLE KEYS */;
/*!40000 ALTER TABLE `properties` ENABLE KEYS */;


# Dumping structure for table larkc.queries
CREATE TABLE IF NOT EXISTS `queries` (
  `idQuery` varchar(45) NOT NULL,
  `Content` varchar(1000) default NULL,
  PRIMARY KEY  (`idQuery`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

# Dumping data for table larkc.queries: ~1 rows (approximately)
/*!40000 ALTER TABLE `queries` DISABLE KEYS */;
/*!40000 ALTER TABLE `queries` ENABLE KEYS */;


# Dumping structure for table larkc.queries_metrics
CREATE TABLE IF NOT EXISTS `queries_metrics` (
  `Queries_idQuery` varchar(45) NOT NULL,
  `Metrics_idMetric` int(10) unsigned NOT NULL,
  `Value` varchar(1000) default NULL,
  `Timestamp` timestamp NOT NULL default '0000-00-00 00:00:00',
  PRIMARY KEY  (`Queries_idQuery`,`Metrics_idMetric`,`Timestamp`),
  KEY `FK_queries_metrics_metrics` (`Metrics_idMetric`),
  CONSTRAINT `FK_queries_metrics_metrics` FOREIGN KEY (`Metrics_idMetric`) REFERENCES `metrics` (`idMetric`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

# Dumping data for table larkc.queries_metrics: ~33 rows (approximately)
/*!40000 ALTER TABLE `queries_metrics` DISABLE KEYS */;
/*!40000 ALTER TABLE `queries_metrics` ENABLE KEYS */;


# Dumping structure for table larkc.queries_metrics_aggregated
CREATE TABLE IF NOT EXISTS `queries_metrics_aggregated` (
  `Queries_idQuery` varchar(45) NOT NULL,
  `Metrics_idMetric` int(10) unsigned NOT NULL,
  `Value` varchar(45) default NULL,
  `Level` tinyint(4) default NULL,
  PRIMARY KEY  (`Queries_idQuery`,`Metrics_idMetric`),
  KEY `FK_queries_metrics_aggregated_metrics` (`Metrics_idMetric`),
  CONSTRAINT `FK_queries_metrics_aggregated_metrics` FOREIGN KEY (`Metrics_idMetric`) REFERENCES `metrics` (`idMetric`),
  CONSTRAINT `FK_queries_metrics_aggregated_queries` FOREIGN KEY (`Queries_idQuery`) REFERENCES `queries` (`idQuery`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

# Dumping data for table larkc.queries_metrics_aggregated: ~0 rows (approximately)
/*!40000 ALTER TABLE `queries_metrics_aggregated` DISABLE KEYS */;
/*!40000 ALTER TABLE `queries_metrics_aggregated` ENABLE KEYS */;


# Dumping structure for table larkc.queries_workflows
CREATE TABLE IF NOT EXISTS `queries_workflows` (
  `Queries_idQuery` varchar(45) NOT NULL,
  `Workflows_idWorkflow` varchar(45) NOT NULL,
  PRIMARY KEY  (`Queries_idQuery`,`Workflows_idWorkflow`),
  KEY `FK_queries_workflows_workflows` (`Workflows_idWorkflow`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

# Dumping data for table larkc.queries_workflows: ~2 rows (approximately)
/*!40000 ALTER TABLE `queries_workflows` DISABLE KEYS */;
/*!40000 ALTER TABLE `queries_workflows` ENABLE KEYS */;


# Dumping structure for table larkc.systems
CREATE TABLE IF NOT EXISTS `systems` (
  `SystemId` varchar(45) NOT NULL default '',
  `SystemName` varchar(45) default NULL,
  `SystemCPUCount` int(10) default NULL,
  `SystemTotalMemory` int(10) default NULL,
  PRIMARY KEY  (`SystemId`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

# Dumping data for table larkc.systems: ~1 rows (approximately)
/*!40000 ALTER TABLE `systems` DISABLE KEYS */;
/*!40000 ALTER TABLE `systems` ENABLE KEYS */;


# Dumping structure for table larkc.systems_metrics
CREATE TABLE IF NOT EXISTS `systems_metrics` (
  `SystemId` varchar(45) NOT NULL default '',
  `metrics_idMetric` int(10) unsigned NOT NULL default '0',
  `Value` varchar(50) default NULL,
  `Timestamp` timestamp NOT NULL default '0000-00-00 00:00:00',
  PRIMARY KEY  (`SystemId`,`metrics_idMetric`,`Timestamp`),
  KEY `FK_system_metrics_metrics` (`metrics_idMetric`),
  CONSTRAINT `FK_system_metrics_metrics` FOREIGN KEY (`metrics_idMetric`) REFERENCES `metrics` (`idMetric`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

# Dumping data for table larkc.systems_metrics: ~1,079 rows (approximately)
/*!40000 ALTER TABLE `systems_metrics` DISABLE KEYS */;
/*!40000 ALTER TABLE `systems_metrics` ENABLE KEYS */;


# Dumping structure for table larkc.vis_settings
CREATE TABLE IF NOT EXISTS `vis_settings` (
  `SettingName` varchar(25) default NULL,
  `SettingValue` varchar(50) default NULL
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

# Dumping data for table larkc.vis_settings: ~2 rows (approximately)
/*!40000 ALTER TABLE `vis_settings` DISABLE KEYS */;
INSERT IGNORE INTO `vis_settings` (`SettingName`, `SettingValue`) VALUES
	('StartDate', '2011-08-16 08:30:00'),
	('EndDate', '2011-08-18 07:10:00');
/*!40000 ALTER TABLE `vis_settings` ENABLE KEYS */;


# Dumping structure for table larkc.workflows
CREATE TABLE IF NOT EXISTS `workflows` (
  `WorkflowId` varchar(45) NOT NULL,
  `WorkflowDescription` varchar(800) NOT NULL,
  PRIMARY KEY  (`WorkflowId`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

# Dumping data for table larkc.workflows: ~1 rows (approximately)
/*!40000 ALTER TABLE `workflows` DISABLE KEYS */;
/*!40000 ALTER TABLE `workflows` ENABLE KEYS */;


# Dumping structure for table larkc.workflows_metrics
CREATE TABLE IF NOT EXISTS `workflows_metrics` (
  `Workflows_idWorkflow` varchar(45) NOT NULL,
  `Metrics_idMetric` int(10) unsigned NOT NULL,
  `Value` varchar(45) default NULL,
  `Timestamp` timestamp NOT NULL default '0000-00-00 00:00:00',
  PRIMARY KEY  (`Workflows_idWorkflow`,`Metrics_idMetric`,`Timestamp`),
  KEY `FK_workflows_metrics_metrics` (`Metrics_idMetric`),
  CONSTRAINT `FK_workflows_metrics_metrics` FOREIGN KEY (`Metrics_idMetric`) REFERENCES `metrics` (`idMetric`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

# Dumping data for table larkc.workflows_metrics: ~42 rows (approximately)
/*!40000 ALTER TABLE `workflows_metrics` DISABLE KEYS */;
/*!40000 ALTER TABLE `workflows_metrics` ENABLE KEYS */;


# Dumping structure for table larkc.workflows_metrics_aggregated
CREATE TABLE IF NOT EXISTS `workflows_metrics_aggregated` (
  `Workflows_idWorkflow` varchar(45) NOT NULL,
  `Metrics_idMetric` int(10) unsigned NOT NULL,
  `Value` varchar(45) default NULL,
  `Level` tinyint(4) default NULL,
  PRIMARY KEY  (`Workflows_idWorkflow`,`Metrics_idMetric`),
  KEY `FK_workflows_metrics_aggregated_metrics` (`Metrics_idMetric`),
  CONSTRAINT `FK_workflows_metrics_aggregated_metrics` FOREIGN KEY (`Metrics_idMetric`) REFERENCES `metrics` (`idMetric`),
  CONSTRAINT `FK_workflows_metrics_aggregated_workflow_workflowinstance` FOREIGN KEY (`Workflows_idWorkflow`) REFERENCES `workflows` (`WorkflowId`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

# Dumping data for table larkc.workflows_metrics_aggregated: ~0 rows (approximately)
/*!40000 ALTER TABLE `workflows_metrics_aggregated` DISABLE KEYS */;
/*!40000 ALTER TABLE `workflows_metrics_aggregated` ENABLE KEYS */;


# Dumping structure for table larkc.workflows_plugins
CREATE TABLE IF NOT EXISTS `workflows_plugins` (
  `Workflows_idWorkflow` varchar(45) NOT NULL,
  `idPlugin` varchar(45) NOT NULL,
  PRIMARY KEY  (`Workflows_idWorkflow`,`idPlugin`),
  KEY `FK_workflows_plugins_plugins` (`idPlugin`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

# Dumping data for table larkc.workflows_plugins: ~0 rows (approximately)
/*!40000 ALTER TABLE `workflows_plugins` DISABLE KEYS */;
/*!40000 ALTER TABLE `workflows_plugins` ENABLE KEYS */;
/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
