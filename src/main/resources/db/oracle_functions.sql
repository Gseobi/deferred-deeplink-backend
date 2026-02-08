CREATE OR REPLACE FUNCTION fn_next_click_id
    RETURN VARCHAR2
    IS
    v_seq NUMBER;
BEGIN
    SELECT deeplink_click_id_seq.NEXTVAL
    INTO v_seq
    FROM dual;

    RETURN 'CLK' || LPAD(v_seq, 12, '0');
END;
/