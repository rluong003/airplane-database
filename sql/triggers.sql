DROP TRIGGER IF EXISTS populate ON Pilot;
DROP TRIGGER IF EXISTS populate1 ON Plane;
DROP TRIGGER IF EXISTS populate2 ON Flight;


CREATE LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION id_inc_pilot()
RETURNS "trigger" AS 
$BODY$
BEGIN
New.id = nextval('id_seq_pilot');
Return NEW;
END;
$BODY$
LANGUAGE 'plpgsql' VOLATILE;


CREATE TRIGGER populate BEFORE INSERT
ON Pilot FOR EACH ROW
EXECUTE PROCEDURE id_inc_pilot();



CREATE OR REPLACE FUNCTION id_inc_plane()
RETURNS "trigger" AS 
$BODY$
BEGIN
New.id = nextval('id_seq_plane');
Return NEW;
END;
$BODY$
LANGUAGE 'plpgsql' VOLATILE;


CREATE TRIGGER populate1 BEFORE INSERT
ON Plane FOR EACH ROW
EXECUTE PROCEDURE id_inc_plane();


CREATE OR REPLACE FUNCTION id_inc_flight()
RETURNS "trigger" AS 
$BODY$
BEGIN
New.fnum = nextval('id_seq_flight');
Return NEW;
END;
$BODY$
LANGUAGE 'plpgsql' VOLATILE;


CREATE TRIGGER populate2 BEFORE INSERT
ON Flight FOR EACH ROW
EXECUTE PROCEDURE id_inc_flight();
