
--
-- Structure for table elasticdata_appointment_data_history
--

DROP TABLE IF EXISTS elasticdata_appointment_data_history;
CREATE TABLE elasticdata_appointment_data_history (
id_data_history int AUTO_INCREMENT,
data_type long varchar NOT NULL,
data_value long varchar,
id_ressource long varchar NOT NULL,
PRIMARY KEY (id_data_history)
);
