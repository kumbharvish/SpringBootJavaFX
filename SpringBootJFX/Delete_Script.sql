------------- Delete Script --------------
DELETE FROM APP_USER_DETAILS WHERE USERNAME NOT IN ('admin');
DELETE FROM BILL_ITEM_DETAILS;
DELETE FROM CASH_COUNTER;
DELETE FROM CUSTOMER_BILL_DETAILS;
DELETE FROM CUSTOMER_DETAILS;
DELETE FROM CUSTOMER_PAYMENT_HISTORY;
DELETE FROM EXPENSE_DETAILS;
DELETE FROM MY_STORE_DETAILS;
DELETE FROM MEASUREMENT_UNITS;
DELETE FROM PRODUCT_CATEGORY_DETAILS;
DELETE FROM PRODUCT_DETAILS;
DELETE FROM PRODUCT_PURCHASE_PRICE_HISTORY;
DELETE FROM PRODUCT_STOCK_LEDGER;
DELETE FROM PURCHASE_ENTRY_DETAILS;
DELETE FROM PURCHASE_ENTRY_ITEM_DETAILS;
DELETE FROM SALES_RETURN_DETAILS;
DELETE FROM SALES_RETURN_ITEMS_DETAILS;
DELETE FROM SUPPLIER_DETAILS WHERE SUPPLIER_ID NOT IN (1);
DELETE FROM SUPPLIER_PAYMENT_HISTORY;