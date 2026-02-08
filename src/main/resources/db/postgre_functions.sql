CREATE OR REPLACE FUNCTION fn_next_click_id()
    RETURNS TEXT
    LANGUAGE SQL
AS $$
SELECT 'CLK' || LPAD(NEXTVAL('deeplink_click_id_seq')::TEXT, 12, '0');
$$;