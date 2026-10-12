--
-- PostgreSQL database dump
--


-- Dumped from database version 17.6
-- Dumped by pg_dump version 17.6

SET statement_timeout = 0;
SET lock_timeout = 0;
SET idle_in_transaction_session_timeout = 0;
SET transaction_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SELECT pg_catalog.set_config('search_path', '', false);
SET check_function_bodies = false;
SET xmloption = content;
SET client_min_messages = warning;
SET row_security = off;

ALTER TABLE IF EXISTS ONLY public.user_notifications DROP CONSTRAINT IF EXISTS user_notifications_user_id_fkey;
ALTER TABLE IF EXISTS ONLY public.user_notifications DROP CONSTRAINT IF EXISTS user_notifications_notification_id_fkey;
ALTER TABLE IF EXISTS ONLY public.reviews DROP CONSTRAINT IF EXISTS reviews_user_id_fkey;
ALTER TABLE IF EXISTS ONLY public.reviews DROP CONSTRAINT IF EXISTS reviews_product_id_fkey;
ALTER TABLE IF EXISTS ONLY public.product_variants DROP CONSTRAINT IF EXISTS product_variants_product_id_fkey;
ALTER TABLE IF EXISTS ONLY public.product_categories DROP CONSTRAINT IF EXISTS product_categories_product_id_fkey;
ALTER TABLE IF EXISTS ONLY public.product_categories DROP CONSTRAINT IF EXISTS product_categories_category_id_fkey;
ALTER TABLE IF EXISTS ONLY public.orders DROP CONSTRAINT IF EXISTS orders_user_id_fkey;
ALTER TABLE IF EXISTS ONLY public.order_items DROP CONSTRAINT IF EXISTS order_items_product_variant_id_fkey;
ALTER TABLE IF EXISTS ONLY public.order_items DROP CONSTRAINT IF EXISTS order_items_order_id_fkey;
ALTER TABLE IF EXISTS ONLY public.inventory DROP CONSTRAINT IF EXISTS fkq2yge7ebtfuvwufr6lwfwqy9l;
ALTER TABLE IF EXISTS ONLY public.carts DROP CONSTRAINT IF EXISTS carts_user_id_fkey;
ALTER TABLE IF EXISTS ONLY public.cart_items DROP CONSTRAINT IF EXISTS cart_items_product_variant_id_fkey;
ALTER TABLE IF EXISTS ONLY public.cart_items DROP CONSTRAINT IF EXISTS cart_items_cart_id_fkey;
ALTER TABLE IF EXISTS ONLY public.articles DROP CONSTRAINT IF EXISTS articles_author_id_fkey;
DROP INDEX IF EXISTS public.idx_users_email;
DROP INDEX IF EXISTS public.idx_reviews_product;
DROP INDEX IF EXISTS public.idx_products_slug;
DROP INDEX IF EXISTS public.idx_orders_user;
ALTER TABLE IF EXISTS ONLY public.users DROP CONSTRAINT IF EXISTS users_pkey;
ALTER TABLE IF EXISTS ONLY public.users DROP CONSTRAINT IF EXISTS users_email_key;
ALTER TABLE IF EXISTS ONLY public.user_notifications DROP CONSTRAINT IF EXISTS user_notifications_pkey;
ALTER TABLE IF EXISTS ONLY public.orders DROP CONSTRAINT IF EXISTS uknthkiu7pgmnqnu86i2jyoe2v7;
ALTER TABLE IF EXISTS ONLY public.inventory DROP CONSTRAINT IF EXISTS ukce3rbi3bfstbvvyne34c1dvyv;
ALTER TABLE IF EXISTS ONLY public.reviews DROP CONSTRAINT IF EXISTS uk1nv3auyahyyy79hvtrcqgtfo9;
ALTER TABLE IF EXISTS ONLY public.reviews DROP CONSTRAINT IF EXISTS reviews_user_id_product_id_key;
ALTER TABLE IF EXISTS ONLY public.reviews DROP CONSTRAINT IF EXISTS reviews_pkey;
ALTER TABLE IF EXISTS ONLY public.products DROP CONSTRAINT IF EXISTS products_slug_key;
ALTER TABLE IF EXISTS ONLY public.products DROP CONSTRAINT IF EXISTS products_pkey;
ALTER TABLE IF EXISTS ONLY public.product_variants DROP CONSTRAINT IF EXISTS product_variants_pkey;
ALTER TABLE IF EXISTS ONLY public.product_categories DROP CONSTRAINT IF EXISTS product_categories_pkey;
ALTER TABLE IF EXISTS ONLY public.payments DROP CONSTRAINT IF EXISTS payments_pkey;
ALTER TABLE IF EXISTS ONLY public.orders DROP CONSTRAINT IF EXISTS orders_pkey;
ALTER TABLE IF EXISTS ONLY public.order_items DROP CONSTRAINT IF EXISTS order_items_pkey;
ALTER TABLE IF EXISTS ONLY public.notifications DROP CONSTRAINT IF EXISTS notifications_pkey;
ALTER TABLE IF EXISTS ONLY public.inventory DROP CONSTRAINT IF EXISTS inventory_pkey;
ALTER TABLE IF EXISTS ONLY public.chat_tool_logs DROP CONSTRAINT IF EXISTS chat_tool_logs_pkey;
ALTER TABLE IF EXISTS ONLY public.chat_sessions DROP CONSTRAINT IF EXISTS chat_sessions_pkey;
ALTER TABLE IF EXISTS ONLY public.chat_messages DROP CONSTRAINT IF EXISTS chat_messages_pkey;
ALTER TABLE IF EXISTS ONLY public.categories DROP CONSTRAINT IF EXISTS categories_slug_key;
ALTER TABLE IF EXISTS ONLY public.categories DROP CONSTRAINT IF EXISTS categories_pkey;
ALTER TABLE IF EXISTS ONLY public.carts DROP CONSTRAINT IF EXISTS carts_user_id_key;
ALTER TABLE IF EXISTS ONLY public.carts DROP CONSTRAINT IF EXISTS carts_pkey;
ALTER TABLE IF EXISTS ONLY public.cart_items DROP CONSTRAINT IF EXISTS cart_items_pkey;
ALTER TABLE IF EXISTS ONLY public.articles DROP CONSTRAINT IF EXISTS articles_slug_key;
ALTER TABLE IF EXISTS ONLY public.articles DROP CONSTRAINT IF EXISTS articles_pkey;
ALTER TABLE IF EXISTS public.users ALTER COLUMN id DROP DEFAULT;
ALTER TABLE IF EXISTS public.reviews ALTER COLUMN id DROP DEFAULT;
ALTER TABLE IF EXISTS public.products ALTER COLUMN id DROP DEFAULT;
ALTER TABLE IF EXISTS public.product_variants ALTER COLUMN id DROP DEFAULT;
ALTER TABLE IF EXISTS public.orders ALTER COLUMN id DROP DEFAULT;
ALTER TABLE IF EXISTS public.order_items ALTER COLUMN id DROP DEFAULT;
ALTER TABLE IF EXISTS public.notifications ALTER COLUMN id DROP DEFAULT;
ALTER TABLE IF EXISTS public.categories ALTER COLUMN id DROP DEFAULT;
ALTER TABLE IF EXISTS public.carts ALTER COLUMN id DROP DEFAULT;
ALTER TABLE IF EXISTS public.cart_items ALTER COLUMN id DROP DEFAULT;
ALTER TABLE IF EXISTS public.articles ALTER COLUMN id DROP DEFAULT;
DROP SEQUENCE IF EXISTS public.users_id_seq;
DROP TABLE IF EXISTS public.users;
DROP TABLE IF EXISTS public.user_notifications;
DROP SEQUENCE IF EXISTS public.reviews_id_seq;
DROP TABLE IF EXISTS public.reviews;
DROP SEQUENCE IF EXISTS public.products_id_seq;
DROP TABLE IF EXISTS public.products;
DROP SEQUENCE IF EXISTS public.product_variants_id_seq;
DROP TABLE IF EXISTS public.product_variants;
DROP TABLE IF EXISTS public.product_categories;
DROP TABLE IF EXISTS public.payments;
DROP SEQUENCE IF EXISTS public.orders_id_seq;
DROP TABLE IF EXISTS public.orders;
DROP SEQUENCE IF EXISTS public.order_items_id_seq;
DROP TABLE IF EXISTS public.order_items;
DROP SEQUENCE IF EXISTS public.notifications_id_seq;
DROP TABLE IF EXISTS public.notifications;
DROP TABLE IF EXISTS public.inventory;
DROP TABLE IF EXISTS public.chat_tool_logs;
DROP TABLE IF EXISTS public.chat_sessions;
DROP TABLE IF EXISTS public.chat_messages;
DROP SEQUENCE IF EXISTS public.categories_id_seq;
DROP TABLE IF EXISTS public.categories;
DROP SEQUENCE IF EXISTS public.carts_id_seq;
DROP TABLE IF EXISTS public.carts;
DROP SEQUENCE IF EXISTS public.cart_items_id_seq;
DROP TABLE IF EXISTS public.cart_items;
DROP SEQUENCE IF EXISTS public.articles_id_seq;
DROP TABLE IF EXISTS public.articles;
SET default_tablespace = '';

SET default_table_access_method = heap;

--
-- Name: articles; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.articles (
    id bigint NOT NULL,
    author_id bigint,
    title character varying(255),
    slug character varying(255),
    content text,
    created_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    updated_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP
);


--
-- Name: articles_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.articles_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: articles_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.articles_id_seq OWNED BY public.articles.id;


--
-- Name: cart_items; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.cart_items (
    id bigint NOT NULL,
    cart_id bigint,
    product_variant_id bigint,
    quantity integer NOT NULL,
    price numeric(38,2) NOT NULL,
    CONSTRAINT cart_items_quantity_check CHECK ((quantity > 0))
);


--
-- Name: cart_items_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.cart_items_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: cart_items_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.cart_items_id_seq OWNED BY public.cart_items.id;


--
-- Name: carts; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.carts (
    id bigint NOT NULL,
    user_id bigint,
    created_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    updated_at timestamp(6) without time zone
);


--
-- Name: carts_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.carts_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: carts_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.carts_id_seq OWNED BY public.carts.id;


--
-- Name: categories; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.categories (
    id bigint NOT NULL,
    name character varying(255) NOT NULL,
    slug character varying(255) NOT NULL,
    image_url text,
    description text,
    created_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    updated_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP
);


--
-- Name: categories_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.categories_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: categories_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.categories_id_seq OWNED BY public.categories.id;


--
-- Name: chat_messages; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.chat_messages (
    id bigint NOT NULL,
    content text NOT NULL,
    created_at timestamp(6) without time zone,
    model_name character varying(100),
    role character varying(20) NOT NULL,
    session_id uuid NOT NULL
);


--
-- Name: chat_messages_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

ALTER TABLE public.chat_messages ALTER COLUMN id ADD GENERATED BY DEFAULT AS IDENTITY (
    SEQUENCE NAME public.chat_messages_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- Name: chat_sessions; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.chat_sessions (
    id uuid NOT NULL,
    channel character varying(20) NOT NULL,
    created_at timestamp(6) without time zone,
    status character varying(20) NOT NULL,
    updated_at timestamp(6) without time zone,
    user_id bigint
);


--
-- Name: chat_tool_logs; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.chat_tool_logs (
    id bigint NOT NULL,
    created_at timestamp(6) without time zone,
    duration_ms bigint,
    error_code character varying(100),
    session_id uuid NOT NULL,
    status character varying(20) NOT NULL,
    tool_arguments text,
    tool_name character varying(100) NOT NULL,
    tool_result text,
    user_id bigint
);


--
-- Name: chat_tool_logs_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

ALTER TABLE public.chat_tool_logs ALTER COLUMN id ADD GENERATED BY DEFAULT AS IDENTITY (
    SEQUENCE NAME public.chat_tool_logs_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- Name: inventory; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.inventory (
    id bigint NOT NULL,
    last_updated timestamp(6) without time zone,
    min_quantity integer NOT NULL,
    quantity integer NOT NULL,
    warehouse character varying(255) NOT NULL,
    product_id bigint NOT NULL
);


--
-- Name: inventory_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

ALTER TABLE public.inventory ALTER COLUMN id ADD GENERATED BY DEFAULT AS IDENTITY (
    SEQUENCE NAME public.inventory_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- Name: notifications; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.notifications (
    id bigint NOT NULL,
    title character varying(255),
    content text,
    created_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP
);


--
-- Name: notifications_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.notifications_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: notifications_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.notifications_id_seq OWNED BY public.notifications.id;


--
-- Name: order_items; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.order_items (
    id bigint NOT NULL,
    order_id bigint,
    product_variant_id bigint,
    quantity integer NOT NULL,
    unit_price numeric(38,2) NOT NULL,
    created_at timestamp(6) without time zone,
    CONSTRAINT order_items_quantity_check CHECK ((quantity > 0))
);


--
-- Name: order_items_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.order_items_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: order_items_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.order_items_id_seq OWNED BY public.order_items.id;


--
-- Name: orders; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.orders (
    id bigint NOT NULL,
    user_id bigint,
    total_cost numeric(38,2) NOT NULL,
    shipping_cost numeric(38,2),
    payment_method character varying(255),
    status character varying(255),
    created_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    updated_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    address character varying(255) NOT NULL,
    cancelled_at timestamp(6) without time zone,
    compartment_id character varying(255),
    confirmed_at timestamp(6) without time zone,
    delivered_at timestamp(6) without time zone,
    delivery_status character varying(255),
    discount numeric(38,2),
    email character varying(255) NOT NULL,
    full_name character varying(255) NOT NULL,
    locker_id character varying(255),
    locker_order_id character varying(255),
    note character varying(255),
    order_number character varying(255) NOT NULL,
    payment_status character varying(255),
    phone character varying(255) NOT NULL,
    province character varying(255) NOT NULL,
    province_code character varying(255) NOT NULL,
    recipient_otp character varying(255),
    sender_otp character varying(255),
    shipped_at timestamp(6) without time zone,
    shipping_fee numeric(38,2),
    shipping_method character varying(255) NOT NULL,
    subtotal numeric(38,2) NOT NULL,
    total numeric(38,2) NOT NULL,
    total_price numeric(38,2) NOT NULL,
    voucher_code character varying(255),
    ward character varying(255) NOT NULL,
    ward_code character varying(255) NOT NULL
);


--
-- Name: orders_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.orders_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: orders_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.orders_id_seq OWNED BY public.orders.id;


--
-- Name: payments; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.payments (
    id bigint NOT NULL,
    amount numeric(38,2) NOT NULL,
    created_at timestamp(6) without time zone,
    order_id bigint NOT NULL,
    payment_method character varying(255),
    status character varying(255) NOT NULL,
    transaction_id character varying(255),
    CONSTRAINT payments_status_check CHECK (((status)::text = ANY ((ARRAY['PENDING'::character varying, 'SUCCESS'::character varying, 'FAILED'::character varying])::text[])))
);


--
-- Name: payments_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

ALTER TABLE public.payments ALTER COLUMN id ADD GENERATED BY DEFAULT AS IDENTITY (
    SEQUENCE NAME public.payments_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- Name: product_categories; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.product_categories (
    product_id bigint NOT NULL,
    category_id bigint NOT NULL
);


--
-- Name: product_variants; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.product_variants (
    id bigint NOT NULL,
    product_id bigint,
    variant_name character varying(255),
    description text,
    specifications text,
    image_url character varying(255),
    price numeric(12,2) NOT NULL,
    discount_price numeric(12,2),
    in_stock integer DEFAULT 0,
    is_available boolean DEFAULT true,
    created_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    updated_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT product_variants_in_stock_check CHECK ((in_stock >= 0))
);


--
-- Name: product_variants_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.product_variants_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: product_variants_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.product_variants_id_seq OWNED BY public.product_variants.id;


--
-- Name: products; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.products (
    id bigint NOT NULL,
    name character varying(255) NOT NULL,
    slug character varying(255) NOT NULL,
    avg_rating double precision DEFAULT 0,
    sold_quantity integer DEFAULT 0,
    created_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    updated_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP
);


--
-- Name: products_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.products_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: products_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.products_id_seq OWNED BY public.products.id;


--
-- Name: reviews; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.reviews (
    id bigint NOT NULL,
    user_id bigint,
    product_id bigint,
    rating integer,
    comment text,
    created_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT reviews_rating_check CHECK (((rating >= 1) AND (rating <= 5)))
);


--
-- Name: reviews_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.reviews_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: reviews_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.reviews_id_seq OWNED BY public.reviews.id;


--
-- Name: user_notifications; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.user_notifications (
    user_id bigint NOT NULL,
    notification_id bigint NOT NULL,
    is_read boolean DEFAULT false
);


--
-- Name: users; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.users (
    id bigint NOT NULL,
    email character varying(255) NOT NULL,
    password character varying(255),
    full_name character varying(255),
    phone character varying(255),
    province character varying(255),
    ward character varying(255),
    detailed_address character varying(255),
    gender character varying(10),
    birth_date date,
    role character varying(50) DEFAULT 'ROLE_USER'::character varying,
    is_active boolean DEFAULT true,
    created_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    updated_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    email_verified boolean
);


--
-- Name: users_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.users_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: users_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.users_id_seq OWNED BY public.users.id;


--
-- Name: articles id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.articles ALTER COLUMN id SET DEFAULT nextval('public.articles_id_seq'::regclass);


--
-- Name: cart_items id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.cart_items ALTER COLUMN id SET DEFAULT nextval('public.cart_items_id_seq'::regclass);


--
-- Name: carts id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.carts ALTER COLUMN id SET DEFAULT nextval('public.carts_id_seq'::regclass);


--
-- Name: categories id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.categories ALTER COLUMN id SET DEFAULT nextval('public.categories_id_seq'::regclass);


--
-- Name: notifications id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.notifications ALTER COLUMN id SET DEFAULT nextval('public.notifications_id_seq'::regclass);


--
-- Name: order_items id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.order_items ALTER COLUMN id SET DEFAULT nextval('public.order_items_id_seq'::regclass);


--
-- Name: orders id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.orders ALTER COLUMN id SET DEFAULT nextval('public.orders_id_seq'::regclass);


--
-- Name: product_variants id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.product_variants ALTER COLUMN id SET DEFAULT nextval('public.product_variants_id_seq'::regclass);


--
-- Name: products id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.products ALTER COLUMN id SET DEFAULT nextval('public.products_id_seq'::regclass);


--
-- Name: reviews id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.reviews ALTER COLUMN id SET DEFAULT nextval('public.reviews_id_seq'::regclass);


--
-- Name: users id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.users ALTER COLUMN id SET DEFAULT nextval('public.users_id_seq'::regclass);


--
-- Data for Name: articles; Type: TABLE DATA; Schema: public; Owner: -
--



--
-- Data for Name: cart_items; Type: TABLE DATA; Schema: public; Owner: -
--



--
-- Data for Name: carts; Type: TABLE DATA; Schema: public; Owner: -
--

INSERT INTO public.carts VALUES (2, 7, '2026-10-12 02:27:32.943614', '2026-10-12 02:27:32.943614');


--
-- Data for Name: categories; Type: TABLE DATA; Schema: public; Owner: -
--

INSERT INTO public.categories VALUES (1, 'Arduino & Modules', 'arduino-modules', '/images/ArduinoUnoR3.jpg', 'Bo máº¡ch Arduino vÃ  cÃ¡c module má»Ÿ rá»™ng', '2026-10-10 17:58:48.176768', '2026-10-10 17:58:48.176768');
INSERT INTO public.categories VALUES (2, 'Raspberry Pi', 'raspberry-pi', '/images/raspberry-pi-5-6.jpg', 'MÃ¡y tÃ­nh nhÃºng Raspberry Pi vÃ  phá»¥ kiá»‡n', '2026-10-10 17:58:48.176768', '2026-10-10 17:58:48.176768');
INSERT INTO public.categories VALUES (3, 'Linh kiá»‡n Ä‘iá»‡n tá»­', 'linh-kien-dien-tu', '/images/resistores.jpg', 'Äiá»‡n trá»Ÿ, tá»¥ Ä‘iá»‡n, transistor, IC vÃ  cÃ¡c linh kiá»‡n cÆ¡ báº£n', '2026-10-10 17:58:48.176768', '2026-10-10 17:58:48.176768');
INSERT INTO public.categories VALUES (4, 'Cáº£m biáº¿n', 'cam-bien', '/images/cam-bien-vat-can-hong-ngoai-fm52-5.jpg', 'CÃ¡c loáº¡i cáº£m biáº¿n nhiá»‡t Ä‘á»™, Ä‘á»™ áº©m, khoáº£ng cÃ¡ch, Ã¡nh sÃ¡ng', '2026-10-10 17:58:48.176768', '2026-10-10 17:58:48.176768');
INSERT INTO public.categories VALUES (5, 'Module nguá»“n', 'module-nguon', '/images/pin-18650__T7hrNFf7Uz.webp', 'Module nguá»“n, sáº¡c pin, Buck/Boost converter', '2026-10-10 17:58:48.176768', '2026-10-10 17:58:48.176768');
INSERT INTO public.categories VALUES (6, 'Module truyá»n thÃ´ng', 'module-truyen-thong', '/images/nodemcu-esp32-01.webp', 'WiFi, Bluetooth, LoRa, RF, GSM modules', '2026-10-10 17:58:48.176768', '2026-10-10 17:58:48.176768');
INSERT INTO public.categories VALUES (7, 'MÃ n hÃ¬nh hiá»ƒn thá»‹', 'man-hinh-hien-thi', '/images/man-hinh-lcd-oled-0-96-inch-giao-tiep-i2c-white-9w56-1.jpg', 'LCD, OLED, LED Matrix, TFT displays', '2026-10-10 17:58:48.176768', '2026-10-10 17:58:48.176768');
INSERT INTO public.categories VALUES (8, 'Motor & Driver', 'motor-driver', '/images/dong-co-servo-sg90-180-do-rk7a-1.jpg', 'Äá»™ng cÆ¡ vÃ  máº¡ch Ä‘iá»u khiá»ƒn Ä‘á»™ng cÆ¡', '2026-10-10 17:58:48.176768', '2026-10-10 17:58:48.176768');


--
-- Data for Name: chat_messages; Type: TABLE DATA; Schema: public; Owner: -
--

INSERT INTO public.chat_messages VALUES (1, 'TÃ¬m Arduino Uno R3', '2026-10-11 00:00:31.236171', NULL, 'user', '1f68bb4a-c613-42ec-9393-82d6d172bcc1');
INSERT INTO public.chat_messages VALUES (2, '*(Ollama local chÆ°a báº­t - Pháº£n há»“i tá»± Ä‘á»™ng)*
Hiá»‡n táº¡i chÆ°a tÃ¬m tháº¥y linh kiá»‡n nÃ o khá»›p chÃ­nh xÃ¡c vá»›i tá»« khÃ³a "TÃ¬m Arduino Uno R3". Báº¡n vui lÃ²ng kiá»ƒm tra láº¡i tÃªn linh kiá»‡n hoáº·c báº­t Ollama (`ollama run qwen2.5:7b`) Ä‘á»ƒ nháº­n tÆ° váº¥n chi tiáº¿t.', '2026-10-11 00:00:31.396428', 'qwen2.5:7b', 'assistant', '1f68bb4a-c613-42ec-9393-82d6d172bcc1');
INSERT INTO public.chat_messages VALUES (3, 'Arduino', '2026-10-11 00:05:13.645852', NULL, 'user', '1b2fe7a4-7b5a-4802-a1cd-37e34c203116');
INSERT INTO public.chat_messages VALUES (4, '*(Ollama local chÆ°a báº­t - Pháº£n há»“i tá»± Ä‘á»™ng)*
TÃ´i Ä‘Ã£ tÃ¬m tháº¥y 3 sáº£n pháº©m phÃ¹ há»£p vá»›i yÃªu cáº§u cá»§a báº¡n trong cÆ¡ sá»Ÿ dá»¯ liá»‡u.', '2026-10-11 00:05:13.697202', 'qwen2.5:7b', 'assistant', '1b2fe7a4-7b5a-4802-a1cd-37e34c203116');
INSERT INTO public.chat_messages VALUES (5, 'tá»“n kho cáº£nh bÃ¡o', '2026-10-11 00:05:22.987136', NULL, 'user', '6ab8de1b-aefa-4cab-860b-c6be670ba998');
INSERT INTO public.chat_messages VALUES (6, 'Tá»« chá»‘i truy cáº­p: Chá»‰ Quáº£n trá»‹ viÃªn (Admin) má»›i cÃ³ quyá»n truy váº¥n sá»‘ liá»‡u tá»“n kho cáº£nh bÃ¡o.', '2026-10-11 00:05:22.999419', 'qwen2.5:7b', 'assistant', '6ab8de1b-aefa-4cab-860b-c6be670ba998');
INSERT INTO public.chat_messages VALUES (7, 'TÆ° váº¥n cho tÃ´i vá» bo máº¡ch Arduino Uno R3 vÃ  giÃ¡ bÃ¡n cá»§a nÃ³', '2026-10-11 00:12:07.691086', NULL, 'user', '6dccf298-3472-4b43-a1e8-26d972a07804');
INSERT INTO public.chat_messages VALUES (8, 'Arduino Uno R3 cÃ³ hai biáº¿n thá»ƒ chÃ­nh:

1. **Arduino Uno R3 - ChÃ­nh hÃ£ng**:
   - **GiÃ¡ bÃ¡n**: 180,000 VND
   - **GiÃ¡ bÃ¡n giáº£m giÃ¡**: 165,000 VND
   - **Tá»“n kho**: 45

2. **Arduino Uno R3 - Compatible**:
   - **GiÃ¡ bÃ¡n**: 120,000 VND
   - **GiÃ¡ bÃ¡n giáº£m giÃ¡**: 110,000 VND
   - **Tá»“n kho**: 120

Báº¡n cÃ³ thá»ƒ chá»n biáº¿n thá»ƒ phÃ¹ há»£p vá»›i nhu cáº§u cá»§a mÃ¬nh.', '2026-10-11 00:12:11.241829', 'qwen2.5:7b-instruct', 'assistant', '6dccf298-3472-4b43-a1e8-26d972a07804');
INSERT INTO public.chat_messages VALUES (9, 'Sáº£n pháº©m Ä‘Ã³ cÃ²n bao nhiÃªu cÃ¡i trong kho táº¥t cáº£?', '2026-10-11 00:12:32.334511', NULL, 'user', '6dccf298-3472-4b43-a1e8-26d972a07804');
INSERT INTO public.chat_messages VALUES (10, 'Xin lá»—i, tÃ´i khÃ´ng tÃ¬m tháº¥y thÃ´ng tin chÃ­nh xÃ¡c vá» sáº£n pháº©m Arduino Uno R3 trong kho. HÃ£y Ä‘á»ƒ tÃ´i kiá»ƒm tra láº¡i vá»›i sá»‘ lÆ°á»£ng tá»•ng cá»™ng cá»§a cáº£ hai biáº¿n thá»ƒ.

```markdown
**ThÃ´ng tin tá»•ng há»£p:**
- **Arduino Uno R3 - ChÃ­nh hÃ£ng**: 45 cÃ¡i
- **Arduino Uno R3 - Compatible**: 120 cÃ¡i

**Tá»•ng sá»‘ trong kho**: 165 cÃ¡i
```

Náº¿u báº¡n cáº§n thÃ´ng tin chi tiáº¿t hÆ¡n hoáº·c cÃ³ báº¥t ká»³ cÃ¢u há»i nÃ o khÃ¡c, hÃ£y cho tÃ´i biáº¿t nhÃ©!', '2026-10-11 00:12:35.123743', 'qwen2.5:7b-instruct', 'assistant', '6dccf298-3472-4b43-a1e8-26d972a07804');
INSERT INTO public.chat_messages VALUES (11, 'Kiá»ƒm tra danh sÃ¡ch linh kiá»‡n sáº¯p háº¿t hÃ ng', '2026-10-11 00:22:46.513076', NULL, 'user', '6c552ce0-10ce-4a88-9905-9191abeb568e');
INSERT INTO public.chat_messages VALUES (12, 'Xin lá»—i, nhÆ°ng tÃ´i khÃ´ng cÃ³ quyá»n truy cáº­p vÃ o danh sÃ¡ch linh kiá»‡n sáº¯p háº¿t hÃ ng. Báº¡n cÃ³ thá»ƒ liÃªn há»‡ vá»›i Ä‘á»™i ngÅ©Admin Ä‘á»ƒ kiá»ƒm tra thÃ´ng tin nÃ y nhÃ©.', '2026-10-11 00:22:50.862508', 'qwen2.5:7b-instruct', 'assistant', '6c552ce0-10ce-4a88-9905-9191abeb568e');
INSERT INTO public.chat_messages VALUES (13, 'máº·t hÃ ng nÃ o bÃ¡n cháº¡y', '2026-10-11 00:23:30.176987', NULL, 'user', '6c552ce0-10ce-4a88-9905-9191abeb568e');
INSERT INTO public.chat_messages VALUES (14, 'Äá»ƒ kiá»ƒm tra máº·t hÃ ng bÃ¡n cháº¡y, tÃ´i cáº§n tra cá»©u dá»¯ liá»‡u bÃ¡n hÃ ng. Tuy nhiÃªn, chá»©c nÄƒng hiá»‡n táº¡i cá»§a tÃ´i khÃ´ng há»— trá»£ viá»‡c nÃ y. Báº¡n cÃ³ thá»ƒ tham kháº£o bÃ¡o cÃ¡o bÃ¡n hÃ ng tá»« há»‡ thá»‘ng quáº£n lÃ½ bÃ¡n hÃ ng cá»§a báº¡n hoáº·c liÃªn há»‡ vá»›i Ä‘á»™i ngÅ©Admin Ä‘á»ƒ Ä‘Æ°á»£c há»— trá»£.

Náº¿u báº¡n muá»‘n kiá»ƒm tra sá»‘ lÆ°á»£ng tá»“n kho cá»§a má»™t máº·t hÃ ng cá»¥ thá»ƒ, tÃ´i cÃ³ thá»ƒ giÃºp báº¡n. Vui lÃ²ng cung cáº¥p tÃªn hoáº·c ID cá»§a sáº£n pháº©m báº¡n quan tÃ¢m.', '2026-10-11 00:23:32.230694', 'qwen2.5:7b-instruct', 'assistant', '6c552ce0-10ce-4a88-9905-9191abeb568e');
INSERT INTO public.chat_messages VALUES (15, 'Nhung linh kien nao sap het hang trong kho?', '2026-10-11 00:37:06.579126', NULL, 'user', 'a259831b-3efd-48eb-901f-882921850fde');
INSERT INTO public.chat_messages VALUES (16, 'Hiá»‡n táº¡i, khÃ´ng cÃ³ linh kiá»‡n nÃ o trong kho sáº¯p háº¿t hÃ ng. Äá»u cÃ²n Ä‘á»§ hÆ¡n 10 Ä‘Æ¡n vá»‹. Náº¿u báº¡n cáº§n cáº­p nháº­t thÃ´ng tin nÃ y trong tÆ°Æ¡ng lai, hÃ£y cho tÃ´i biáº¿t nhÃ©!', '2026-10-11 00:37:11.048333', 'qwen2.5:7b-instruct', 'assistant', 'a259831b-3efd-48eb-901f-882921850fde');
INSERT INTO public.chat_messages VALUES (17, 'Toi la quan ly kho, hay liet ke nhung linh kien co so luong ton kho duoi 35 cai?', '2026-10-11 00:40:30.778641', NULL, 'user', '9f6b9ae2-4460-4879-82c9-eed921711021');
INSERT INTO public.chat_messages VALUES (18, 'Dá»±a trÃªn thÃ´ng tin tá»« há»‡ thá»‘ng, cÃ³ 3 loáº¡i linh kiá»‡n cÃ³ sá»‘ lÆ°á»£ng tá»“n kho dÆ°á»›i 35 Ä‘Æ¡n vá»‹:

1. **Raspberry Pi 4 Model B 4GB - Kit Ä‘áº§y Ä‘á»§**
   - Sá»‘ lÆ°á»£ng tá»“n kho: 25 Ä‘Æ¡n vá»‹
   - GiÃ¡ bÃ¡n: 1,850,000 VND

2. **Raspberry Pi 3 Model B+ - Kit cÆ¡ báº£n**
   - Sá»‘ lÆ°á»£ng tá»“n kho: 30 Ä‘Æ¡n vá»‹
   - GiÃ¡ bÃ¡n: 1,250,000 VND

3. **Arduino Mega 2560**
   - Sá»‘ lÆ°á»£ng tá»“n kho: 35 Ä‘Æ¡n vá»‹ (Ä‘áº¡t ngÆ°á»¡ng)
   - GiÃ¡ bÃ¡n: 280,000 VND

HÃ£y kiá»ƒm tra láº¡i sá»‘ lÆ°á»£ng tá»“n kho Ä‘á»ƒ Ä‘áº£m báº£o thÃ´ng tin chÃ­nh xÃ¡c.', '2026-10-11 00:40:34.673126', 'qwen2.5:7b-instruct', 'assistant', '9f6b9ae2-4460-4879-82c9-eed921711021');
INSERT INTO public.chat_messages VALUES (19, 'Nhung linh kien nao dang ban chay nhat hien nay?', '2026-10-11 00:41:50.102155', NULL, 'user', '54b05f91-0a61-4dc6-8b18-5b0ad9edb715');
INSERT INTO public.chat_messages VALUES (20, 'DÆ°á»›i Ä‘Ã¢y lÃ  top 5 sáº£n pháº©m bÃ¡n cháº¡y nháº¥t hiá»‡n nay:

1. **TP4056 - Module sáº¡c pin Lithium** - Sá»‘ lÆ°á»£ng Ä‘Ã£ bÃ¡n: 2450
2. **OLED 0.96" I2C 128x64** - Sá»‘ lÆ°á»£ng Ä‘Ã£ bÃ¡n: 2340
3. **HC-SR04 - Cáº£m biáº¿n siÃªu Ã¢m** - Sá»‘ lÆ°á»£ng Ä‘Ã£ bÃ¡n: 2230
4. **Äá»™ng cÆ¡ Servo SG90** - Sá»‘ lÆ°á»£ng Ä‘Ã£ bÃ¡n: 2120
5. **ESP32 DevKit V1** - Sá»‘ lÆ°á»£ng Ä‘Ã£ bÃ¡n: 2100

Cáº£m Æ¡n báº¡n Ä‘Ã£ quan tÃ¢m! Náº¿u báº¡n cáº§n thÃªm thÃ´ng tin hoáº·c há»— trá»£ gÃ¬ ná»¯a, Ä‘á»«ng ngáº§n ngáº¡i liÃªn há»‡ vá»›i chÃºng tÃ´i.', '2026-10-11 00:41:53.577262', 'qwen2.5:7b-instruct', 'assistant', '54b05f91-0a61-4dc6-8b18-5b0ad9edb715');
INSERT INTO public.chat_messages VALUES (21, 'Toi muon xem danh sach linh kien sap het hang trong kho', '2026-10-11 00:49:27.264983', NULL, 'user', '34766809-6c47-4dc0-87fa-ce86aedac5a9');
INSERT INTO public.chat_messages VALUES (22, 'Xin lá»—i, hiá»‡n táº¡i chÃºng tÃ´i khÃ´ng thá»ƒ cung cáº¥p danh sÃ¡ch linh kiá»‡n sáº¯p háº¿t hÃ ng do háº¡n cháº¿ vá» quyá»n truy cáº­p. Báº¡n cÃ³ thá»ƒ liÃªn há»‡ vá»›i bá»™ pháº­n há»— trá»£ cá»§a chÃºng tÃ´i náº¿u báº¡n muá»‘n biáº¿t thÃªm thÃ´ng tin.', '2026-10-11 00:49:32.147177', 'qwen2.5:7b-instruct', 'assistant', '34766809-6c47-4dc0-87fa-ce86aedac5a9');
INSERT INTO public.chat_messages VALUES (23, 'Kiá»ƒm tra danh sÃ¡ch linh kiá»‡n sáº¯p háº¿t hÃ ng', '2026-10-11 00:54:08.552497', NULL, 'user', '3eefffa1-a4f9-41e4-aeb0-26ad15c4c30c');
INSERT INTO public.chat_messages VALUES (24, 'Hiá»‡n táº¡i, há»‡ thá»‘ng khÃ´ng cÃ³ linh kiá»‡n nÃ o sáº¯p háº¿t hÃ ng. Tuy nhiÃªn, ngÆ°á»¡ng cáº£nh bÃ¡o hiá»‡n táº¡i lÃ  khi sá»‘ lÆ°á»£ng cÃ²n láº¡i dÆ°á»›i 10 Ä‘Æ¡n vá»‹. Báº¡n cÃ³ muá»‘n Ä‘iá»u chá»‰nh ngÆ°á»¡ng cáº£nh bÃ¡o nÃ y khÃ´ng?', '2026-10-11 00:54:10.189789', 'qwen2.5:7b-instruct', 'assistant', '3eefffa1-a4f9-41e4-aeb0-26ad15c4c30c');
INSERT INTO public.chat_messages VALUES (25, 'TÆ° váº¥n bo máº¡ch Arduino Uno R3 vÃ  giÃ¡ bÃ¡n', '2026-10-11 00:54:22.344018', NULL, 'user', '7eff3ca7-571b-49fa-be9e-8fb67148f9e7');
INSERT INTO public.chat_messages VALUES (26, 'Bo máº¡ch Arduino Uno R3 cÃ³ hai loáº¡i chÃ­nh:

1. **Arduino Uno R3 - ChÃ­nh hÃ£ng**:
   - GiÃ¡ bÃ¡n: 180,000 VND (khuyáº¿n mÃ£i: 165,000 VND)
   - Sá»‘ lÆ°á»£ng tá»“n kho: 45
   - Äáº·c Ä‘iá»ƒm: Bo máº¡ch Arduino Uno R3 chÃ­nh hÃ£ng vá»›i chip ATmega328P, 14 digital I/O pins, 6 analog inputs, USB connection. PhÃ¹ há»£p cho ngÆ°á»i má»›i báº¯t Ä‘áº§u há»c láº­p trÃ¬nh nhÃºng.

2. **Arduino Uno R3 - Compatible**:
   - GiÃ¡ bÃ¡n: 120,000 VND (khuyáº¿n mÃ£i: 110,000 VND)
   - Sá»‘ lÆ°á»£ng tá»“n kho: 120
   - Äáº·c Ä‘iá»ƒm: Bo máº¡ch Arduino Uno R3 tÆ°Æ¡ng thÃ­ch, giÃ¡ ráº» hÆ¡n, phÃ¹ há»£p cho há»c táº­p vÃ  thá»­ nghiá»‡m.

Cáº£ hai loáº¡i Ä‘á»u cÃ³ sáºµn trong kho. Báº¡n cÃ³ thá»ƒ tham kháº£o thÃªm thÃ´ng sá»‘ ká»¹ thuáº­t vÃ  lá»±a chá»n sáº£n pháº©m phÃ¹ há»£p.', '2026-10-11 00:54:27.102838', 'qwen2.5:7b-instruct', 'assistant', '7eff3ca7-571b-49fa-be9e-8fb67148f9e7');
INSERT INTO public.chat_messages VALUES (27, 'Kiá»ƒm tra danh sÃ¡ch linh kiá»‡n sáº¯p háº¿t hÃ ng', '2026-10-11 04:11:51.478251', NULL, 'user', '6c552ce0-10ce-4a88-9905-9191abeb568e');
INSERT INTO public.chat_messages VALUES (28, 'Hiá»‡n táº¡i, khÃ´ng cÃ³ linh kiá»‡n nÃ o Ä‘ang sáº¯p háº¿t hÃ ng. Tuy nhiÃªn, chÃºng tÃ´i sáº½ tiáº¿p tá»¥c theo dÃµi Ä‘á»ƒ Ä‘áº£m báº£o luÃ´n cÃ³ sáºµn cÃ¡c sáº£n pháº©m quan trá»ng. Náº¿u báº¡n cÃ³ thÃªm cÃ¢u há»i hoáº·c cáº§n há»— trá»£ gÃ¬ ná»¯a, hÃ£y liÃªn há»‡ vá»›i chÃºng tÃ´i!', '2026-10-11 04:12:00.174226', 'qwen2.5:7b-instruct', 'assistant', '6c552ce0-10ce-4a88-9905-9191abeb568e');
INSERT INTO public.chat_messages VALUES (29, 'so sÃ¡nh cÃ¡c loáº¡i esp32', '2026-10-11 04:29:50.46646', NULL, 'user', '6c552ce0-10ce-4a88-9905-9191abeb568e');
INSERT INTO public.chat_messages VALUES (30, 'Äá»ƒ so sÃ¡nh cÃ¡c loáº¡i ESP32, chÃºng ta cáº§n xÃ¡c Ä‘á»‹nh cÃ¡c sáº£n pháº©m cá»¥ thá»ƒ. Báº¡n cÃ³ thá»ƒ muá»‘n so sÃ¡nh cÃ¡c biáº¿n thá»ƒ ESP32-V2, ESP32-C3, ESP32-H2, hoáº·c ESP32-S2? Hoáº·c cÃ³ thá»ƒ báº¡n Ä‘ang quan tÃ¢m Ä‘áº¿n cÃ¡c thÃ´ng sá»‘ ká»¹ thuáº­t chung cá»§a ESP32? HÃ£y cho tÃ´i biáº¿t chi tiáº¿t hÆ¡n Ä‘á»ƒ tÃ´i cÃ³ thá»ƒ há»— trá»£ báº¡n tá»‘t hÆ¡n.

Náº¿u báº¡n khÃ´ng cÃ³ yÃªu cáº§u cá»¥ thá»ƒ, tÃ´i sáº½ tra cá»©u thÃ´ng tin vá» cÃ¡c loáº¡i ESP32 phá»• biáº¿n.', '2026-10-11 04:29:55.746509', 'qwen2.5:7b-instruct', 'assistant', '6c552ce0-10ce-4a88-9905-9191abeb568e');
INSERT INTO public.chat_messages VALUES (31, 'cÃ¡c loáº¡i shop Ä‘ang bÃ¡n', '2026-10-11 04:30:16.961067', NULL, 'user', '6c552ce0-10ce-4a88-9905-9191abeb568e');
INSERT INTO public.chat_messages VALUES (32, 'DÆ°á»›i Ä‘Ã¢y lÃ  thÃ´ng tin vá» cÃ¡c loáº¡i ESP32 Ä‘ang Ä‘Æ°á»£c bÃ¡n táº¡i cá»­a hÃ ng:

### ESP32 DevKit V1
**PhiÃªn báº£n 30 pins:**
- **GiÃ¡ gá»‘c:** 95,000 VND
- **GiÃ¡ khuyáº¿n mÃ£i:** 89,000 VND
- **Sá»‘ lÆ°á»£ng tá»“n kho:** 180
- **ThÃ´ng sá»‘ ká»¹ thuáº­t:**
  - Chip: ESP32-WROOM-32
  - CPU: Dual-core 240MHz
  - SRAM: 520KB
  - Flash: 4MB
  - WiFi: 802.11 b/g/n
  - Bluetooth: v4.2
  - GPIO: 30 pins

**PhiÃªn báº£n 38 pins:**
- **GiÃ¡ gá»‘c:** 105,000 VND
- **GiÃ¡ khuyáº¿n mÃ£i:** 98,000 VND
- **Sá»‘ lÆ°á»£ng tá»“n kho:** 150
- **ThÃ´ng sá»‘ ká»¹ thuáº­t:**
  - Chip: ESP32-WROOM-32
  - CPU: Dual-core 240MHz
  - SRAM: 520KB
  - Flash: 4MB
  - WiFi: 802.11 b/g/n
  - Bluetooth: v4.2
  - GPIO: 38 pins

Cáº£ hai phiÃªn báº£n Ä‘á»u há»— trá»£ WiFi + Bluetooth tÃ­ch há»£p vÃ  Ä‘á»u ráº¥t phÃ¹ há»£p cho cÃ¡c dá»± Ã¡n IoT. PhiÃªn báº£n 38 pins cÃ³ thÃªm GPIO, phÃ¹ há»£p cho cÃ¡c dá»± Ã¡n phá»©c táº¡p hÆ¡n.', '2026-10-11 04:30:23.667459', 'qwen2.5:7b-instruct', 'assistant', '6c552ce0-10ce-4a88-9905-9191abeb568e');
INSERT INTO public.chat_messages VALUES (33, 'thÃ¡ng trÆ°á»›c bÃ¡n Ä‘Æ°á»£c bao nhiÃªu máº·t hÃ ng', '2026-10-11 04:31:02.929706', NULL, 'user', '6c552ce0-10ce-4a88-9905-9191abeb568e');
INSERT INTO public.chat_messages VALUES (34, 'Äá»ƒ tra cá»©u sá»‘ lÆ°á»£ng máº·t hÃ ng bÃ¡n Ä‘Æ°á»£c trong thÃ¡ng trÆ°á»›c, tÃ´i cáº§n gá»i cÃ´ng cá»¥ `get_top_selling_products` Ä‘á»ƒ xem bÃ¡o cÃ¡o bÃ¡n hÃ ng. Tuy nhiÃªn, chá»©c nÄƒng nÃ y khÃ´ng há»— trá»£ trá»±c tiáº¿p bÃ¡o cÃ¡o theo thÃ¡ng. Thay vÃ o Ä‘Ã³, tÃ´i cÃ³ thá»ƒ kiá»ƒm tra sá»‘ lÆ°á»£ng tá»“n kho cá»§a cÃ¡c sáº£n pháº©m hiá»‡n táº¡i.

Báº¡n muá»‘n kiá»ƒm tra sá»‘ lÆ°á»£ng tá»“n kho cá»§a má»™t máº·t hÃ ng cá»¥ thá»ƒ, hay tÃ´i cÃ³ thá»ƒ tra cá»©u sá»‘ lÆ°á»£ng tá»“n kho cá»§a táº¥t cáº£ cÃ¡c máº·t hÃ ng?

Náº¿u báº¡n muá»‘n, tÃ´i cÃ³ thá»ƒ tra cá»©u sá»‘ lÆ°á»£ng tá»“n kho cá»§a cÃ¡c máº·t hÃ ng bÃ¡n cháº¡y nháº¥t hiá»‡n táº¡i. Báº¡n Ä‘á»“ng Ã½ khÃ´ng?', '2026-10-11 04:31:05.494705', 'qwen2.5:7b-instruct', 'assistant', '6c552ce0-10ce-4a88-9905-9191abeb568e');
INSERT INTO public.chat_messages VALUES (35, 'BÃ¡o cÃ¡o doanh thu thÃ¡ng nÃ y', '2026-10-12 00:46:15.384682', NULL, 'user', '7f7dd076-f943-46f3-abb3-02dd42a086a6');
INSERT INTO public.chat_messages VALUES (36, 'BÃ¡o cÃ¡o doanh thu cho thÃ¡ng 10/2026:

- **Doanh thu:** 0 VNÄ
- **Sá»‘ Ä‘Æ¡n hÃ ng Ä‘Ã£ hoÃ n thÃ nh:** 0
- **Tá»•ng sá»‘ Ä‘Æ¡n hÃ ng:** 0

Trong thÃ¡ng 10 nÃ y, há»‡ thá»‘ng chÆ°a ghi nháº­n báº¥t ká»³ Ä‘Æ¡n hÃ ng nÃ o Ä‘Ã£ hoÃ n thÃ nh hoáº·c doanh thu nÃ o tá»« cÃ¡c giao dá»‹ch. HÃ£y tiáº¿p tá»¥c theo dÃµi vÃ  cáº­p nháº­t thÃ´ng tin Ä‘á»ƒ cÃ³ nhá»¯ng cáº£i thiá»‡n trong kinh doanh.

_Náº¿u báº¡n cáº§n thÃªm thÃ´ng tin chi tiáº¿t vá» cÃ¡c Ä‘Æ¡n hÃ ng hoáº·c cÃ³ báº¥t ká»³ cÃ¢u há»i nÃ o, hÃ£y cho tÃ´i biáº¿t._', '2026-10-12 00:46:25.703479', 'qwen2.5:7b-instruct', 'assistant', '7f7dd076-f943-46f3-abb3-02dd42a086a6');
INSERT INTO public.chat_messages VALUES (37, 'Tu van cho toi bo mach Arduino Uno R3', '2026-10-12 00:58:40.380038', NULL, 'user', '10be8541-b2c6-42f5-a53b-4a640fea4516');
INSERT INTO public.chat_messages VALUES (38, 'Xin lá»—i, hiá»‡n táº¡i sáº£n pháº©m Arduino Uno R3 Ä‘ang háº¿t hÃ ng táº¡i kho cá»§a chÃºng tÃ´i. Tuy nhiÃªn, báº¡n váº«n cÃ³ thá»ƒ cÃ¢n nháº¯c cÃ¡c lá»±a chá»n thay tháº¿ khÃ¡c nhÆ° Arduino Mega 2560 hoáº·c Arduino Nano. TÃ´i sáº½ kiá»ƒm tra sá»‘ lÆ°á»£ng tá»“n kho cho cÃ¡c sáº£n pháº©m nÃ y Ä‘á»ƒ Ä‘Æ°a ra tÆ° váº¥n tá»‘t nháº¥t cho báº¡n.

### Sáº£n pháº©m thay tháº¿:
|| GiÃ¡ bÃ¡n | Loáº¡i |
|---|---|---|
| **Arduino Mega 2560** | **$39.99** | Linh kiá»‡n Ä‘iá»‡n tá»­ |
| **Arduino Nano** | **$24.99** | Linh kiá»‡n Ä‘iá»‡n tá»­ |

### TÃ­nh sáºµn cÃ³:
TÃ´i sáº½ kiá»ƒm tra sá»‘ lÆ°á»£ng tá»“n kho cá»§a cÃ¡c sáº£n pháº©m thay tháº¿ nÃ y.

|| Sáº£n pháº©m | Sá»‘ lÆ°á»£ng tá»“n kho |
|---|---|---|
| **Arduino Mega 2560** | **120** | 
| **Arduino Nano** | **150** | 

Arduino Mega 2560 cÃ³ nhiá»u cá»•ng I/O hÆ¡n vÃ  há»— trá»£ nhiá»u chá»©c nÄƒng máº¡nh máº½ hÆ¡n, trong khi Arduino Nano nhá» gá»n vÃ  phÃ¹ há»£p cho cÃ¡c á»©ng dá»¥ng cÃ³ kÃ­ch thÆ°á»›c nhá». Báº¡n cÃ³ thá»ƒ chá»n sáº£n pháº©m phÃ¹ há»£p vá»›i nhu cáº§u cá»§a mÃ¬nh.

Náº¿u báº¡n cáº§n thÃªm thÃ´ng tin vá» cÃ¡c sáº£n pháº©m nÃ y, hÃ£y cho tÃ´i biáº¿t!', '2026-10-12 00:58:56.919168', 'qwen2.5:7b-instruct', 'assistant', '10be8541-b2c6-42f5-a53b-4a640fea4516');
INSERT INTO public.chat_messages VALUES (39, 'Tu van cho toi bo mach Arduino Uno R3', '2026-10-12 02:06:51.190248', NULL, 'user', 'e5748f0e-03cb-4fd2-9b33-f774bd249306');
INSERT INTO public.chat_messages VALUES (40, '### Bá»™ MÃ¡y Chá»§ Arduino Uno R3

**ThÃ´ng sá»‘ ká»¹ thuáº­t chÃ­nh:**
- **Vi Ä‘iá»u khiá»ƒn:** ATmega328P
- **Äiá»‡n Ã¡p:** **5V**
- **Digital I/O:** 14 (6 PWM)
- **Analog Input:** 6
- **Flash:** 32KB
- **SRAM:** 2KB
- **Clock:** **16MHz**

Bá»™ mÃ¡y chá»§ Arduino Uno R3 phÃ¹ há»£p cho ngÆ°á»i má»›i báº¯t Ä‘áº§u há»c láº­p trÃ¬nh nhÃºng. Sáº£n pháº©m cÃ³ hai phiÃªn báº£n chÃ­nh: **Arduino Uno R3 - ChÃ­nh hÃ£ng** vÃ  **Arduino Uno R3 - Compatible**.

#### Arduino Uno R3 - ChÃ­nh hÃ£ng
- **GiÃ¡ bÃ¡n:** 180.000 VNÄ
- **GiÃ¡ khuyáº¿n mÃ£i:** 165.000 VNÄ
- **Tá»“n kho:** 45

#### Arduino Uno R3 - Compatible
- **GiÃ¡ bÃ¡n:** 120.000 VNÄ
- **GiÃ¡ khuyáº¿n mÃ£i:** 110.000 VNÄ
- **Tá»“n kho:** 120

**Lá»i khuyÃªn:** 

- **Äá»‘i vá»›i ngÆ°á»i má»›i báº¯t Ä‘áº§u:** Báº¡n nÃªn chá»n **Arduino Uno R3 - ChÃ­nh hÃ£ng** vá»›i giÃ¡ Æ°u Ä‘Ã£i 165.000 VNÄ. Sáº£n pháº©m nÃ y Ä‘Æ°á»£c há»— trá»£ tá»‘t hÆ¡n vÃ  cÃ³ Ä‘á»™ tin cáº­y cao.
- **Äá»‘i vá»›i ngÆ°á»i cáº§n tiáº¿t kiá»‡m chi phÃ­:** **Arduino Uno R3 - Compatible** lÃ  lá»±a chá»n phÃ¹ há»£p vá»›i giÃ¡ 110.000 VNÄ, phÃ¹ há»£p cho cÃ¡c dá»± Ã¡n thá»­ nghiá»‡m vÃ  há»c táº­p.

**LÆ°u Ã½:** Sáº£n pháº©m hiá»‡n cÃ³ sáºµn trong kho.

[Product Card](/products/arduino-uno-r3)

Báº¡n cÃ³ muá»‘n biáº¿t thÃªm thÃ´ng tin vá» máº·t hÃ ng nÃ o khÃ¡c khÃ´ng?', '2026-10-12 02:07:07.148178', 'qwen2.5:7b-instruct', 'assistant', 'e5748f0e-03cb-4fd2-9b33-f774bd249306');


--
-- Data for Name: chat_sessions; Type: TABLE DATA; Schema: public; Owner: -
--

INSERT INTO public.chat_sessions VALUES ('1f68bb4a-c613-42ec-9393-82d6d172bcc1', 'web', '2026-10-11 00:00:31.227114', 'active', '2026-10-11 00:00:31.227114', NULL);
INSERT INTO public.chat_sessions VALUES ('1b2fe7a4-7b5a-4802-a1cd-37e34c203116', 'web', '2026-10-11 00:05:13.645852', 'active', '2026-10-11 00:05:13.645852', NULL);
INSERT INTO public.chat_sessions VALUES ('6ab8de1b-aefa-4cab-860b-c6be670ba998', 'web', '2026-10-11 00:05:22.987136', 'active', '2026-10-11 00:05:22.987136', NULL);
INSERT INTO public.chat_sessions VALUES ('6dccf298-3472-4b43-a1e8-26d972a07804', 'web', '2026-10-11 00:12:07.691086', 'active', '2026-10-11 00:12:07.691086', NULL);
INSERT INTO public.chat_sessions VALUES ('6c552ce0-10ce-4a88-9905-9191abeb568e', 'web', '2026-10-11 00:22:46.51207', 'active', '2026-10-11 00:22:46.51207', NULL);
INSERT INTO public.chat_sessions VALUES ('a259831b-3efd-48eb-901f-882921850fde', 'web', '2026-10-11 00:37:06.578597', 'active', '2026-10-11 00:37:06.578597', 4);
INSERT INTO public.chat_sessions VALUES ('9f6b9ae2-4460-4879-82c9-eed921711021', 'web', '2026-10-11 00:40:30.778641', 'active', '2026-10-11 00:40:30.778641', 4);
INSERT INTO public.chat_sessions VALUES ('54b05f91-0a61-4dc6-8b18-5b0ad9edb715', 'web', '2026-10-11 00:41:50.101636', 'active', '2026-10-11 00:41:50.101636', 4);
INSERT INTO public.chat_sessions VALUES ('34766809-6c47-4dc0-87fa-ce86aedac5a9', 'web', '2026-10-11 00:49:27.264983', 'active', '2026-10-11 00:49:27.264983', NULL);
INSERT INTO public.chat_sessions VALUES ('3eefffa1-a4f9-41e4-aeb0-26ad15c4c30c', 'web', '2026-10-11 00:54:08.552497', 'active', '2026-10-11 00:54:08.552497', 4);
INSERT INTO public.chat_sessions VALUES ('7eff3ca7-571b-49fa-be9e-8fb67148f9e7', 'web', '2026-10-11 00:54:22.344018', 'active', '2026-10-11 00:54:22.344018', 4);
INSERT INTO public.chat_sessions VALUES ('7f7dd076-f943-46f3-abb3-02dd42a086a6', 'web', '2026-10-12 00:46:15.376971', 'active', '2026-10-12 00:46:15.376971', 4);
INSERT INTO public.chat_sessions VALUES ('10be8541-b2c6-42f5-a53b-4a640fea4516', 'web', '2026-10-12 00:58:40.379031', 'active', '2026-10-12 00:58:40.379031', NULL);
INSERT INTO public.chat_sessions VALUES ('e5748f0e-03cb-4fd2-9b33-f774bd249306', 'web', '2026-10-12 02:06:51.185234', 'active', '2026-10-12 02:06:51.185234', NULL);


--
-- Data for Name: chat_tool_logs; Type: TABLE DATA; Schema: public; Owner: -
--

INSERT INTO public.chat_tool_logs VALUES (1, '2026-10-11 00:00:31.394828', 11, NULL, '1f68bb4a-c613-42ec-9393-82d6d172bcc1', 'SUCCESS', '{"query":"TÃ¬m Arduino Uno R3"}', 'search_products', '{"query":"TÃ¬m Arduino Uno R3","totalFound":0,"products":[]}', NULL);
INSERT INTO public.chat_tool_logs VALUES (2, '2026-10-11 00:05:13.694385', 26, NULL, '1b2fe7a4-7b5a-4802-a1cd-37e34c203116', 'SUCCESS', '{"query":"Arduino"}', 'search_products', '{"query":"Arduino","totalFound":3,"products":[{"id":1,"name":"Arduino Uno R3","slug":"arduino-uno-r3","avgRating":4.8,"soldQuantity":1250,"minPrice":120000.00,"maxPrice":180000.00,"minDiscountPrice":110000.00,"maxDiscountPrice":165000.00,"defaultImageUrl":null,"totalStock":165,"hasDiscount":true,"categories":[{"id":1,"name":"Arduino & Modules","slug":"arduino-modules","imageUrl":"/images/ArduinoUnoR3.jpg","description":"Bo máº¡ch Arduino vÃ  cÃ¡c module má»Ÿ rá»™ng","createdAt":[2026,10,10,17,58,48,176768000],"updatedAt":[2026,10,10,17,58,48,176768000]}],"variants":[{"id":1,"productId":1,"productName":"Arduino Uno R3","variantName":"Arduino Uno R3 - ChÃ­nh hÃ£ng","description":"Bo máº¡ch Arduino Uno R3 chÃ­nh hÃ£ng vá»›i chip ATmega328P, 14 digital I/O pins, 6 analog inputs, USB connection. PhÃ¹ há»£p cho ngÆ°á»i má»›i báº¯t Ä‘áº§u há»c láº­p trÃ¬nh nhÃºng.","specifications":"Vi Ä‘iá»u khiá»ƒn: ATmega328P | Äiá»‡n Ã¡p: 5V | Digital I/O: 14 (6 PWM) | Analog Input: 6 | Flash: 32KB | SRAM: 2KB | Clock: 16MHz","imageUrl":null,"price":180000.00,"discountPrice":165000.00,"inStock":45,"isAvailable":true,"createdAt":[2026,10,10,17,58,48,184340000],"updatedAt":[2026,10,10,17,58,48,184340000]},{"id":2,"productId":1,"productName":"Arduino Uno R3","variantName":"Arduino Uno R3 - Compatible","description":"Bo máº¡ch Arduino Uno R3 tÆ°Æ¡ng thÃ­ch, giÃ¡ ráº» hÆ¡n, phÃ¹ há»£p cho há»c táº­p vÃ  thá»­ nghiá»‡m.","specifications":"Vi Ä‘iá»u khiá»ƒn: ATmega328P | Äiá»‡n Ã¡p: 5V | Digital I/O: 14 (6 PWM) | Analog Input: 6 | Flash: 32KB | SRAM: 2KB | Clock: 16MHz","imageUrl":null,"price":120000.00,"discountPrice":110000.00,"inStock":120,"isAvailable":true,"createdAt":[2026,10,10,17,58,48,184340000],"updatedAt":[2026,10,10,17,58,48,184340000]}],"createdAt":[2026,10,10,17,58,48,178447000],"updatedAt":[2026,10,10,17,58,48,178447000]},{"id":2,"name":"Arduino Nano V3","slug":"arduino-nano-v3","avgRating":4.7,"soldQuantity":980,"minPrice":65000.00,"maxPrice":85000.00,"minDiscountPrice":59000.00,"maxDiscountPrice":59000.00,"defaultImageUrl":null,"totalStock":280,"hasDiscount":true,"categories":[{"id":1,"name":"Arduino & Modules","slug":"arduino-modules","imageUrl":"/images/ArduinoUnoR3.jpg","description":"Bo máº¡ch Arduino vÃ  cÃ¡c module má»Ÿ rá»™ng","createdAt":[2026,10,10,17,58,48,176768000],"updatedAt":[2026,10,10,17,58,48,176768000]}],"variants":[{"id":3,"productId":2,"productName":"Arduino Nano V3","variantName":"Arduino Nano V3 CH340","description":"Arduino Nano V3 sá»­ dá»¥ng chip CH340, kÃ­ch thÆ°á»›c nhá» gá»n phÃ¹ há»£p cho cÃ¡c dá»± Ã¡n cÃ³ khÃ´ng gian háº¡n cháº¿.","specifications":"Vi Ä‘iá»u khiá»ƒn: ATmega328P | USB: CH340 | Äiá»‡n Ã¡p: 5V | Digital I/O: 14 | Analog Input: 8 | Flash: 32KB","imageUrl":null,"price":65000.00,"discountPrice":59000.00,"inStock":200,"isAvailable":true,"createdAt":[2026,10,10,17,58,48,184340000],"updatedAt":[2026,10,10,17,58,48,184340000]},{"id":4,"productId":2,"productName":"Arduino Nano V3","variantName":"Arduino Nano V3 FTDI","description":"Arduino Nano V3 sá»­ dá»¥ng chip FTDI chÃ­nh hÃ£ng, á»•n Ä‘á»‹nh hÆ¡n trong giao tiáº¿p serial.","specifications":"Vi Ä‘iá»u khiá»ƒn: ATmega328P | USB: FTDI | Äiá»‡n Ã¡p: 5V | Digital I/O: 14 | Analog Input: 8 | Flash: 32KB","imageUrl":null,"price":85000.00,"discountPrice":null,"inStock":80,"isAvailable":true,"createdAt":[2026,10,10,17,58,48,184340000],"updatedAt":[2026,10,10,17,58,48,184340000]}],"createdAt":[2026,10,10,17,58,48,178447000],"updatedAt":[2026,10,10,17,58,48,178447000]},{"id":3,"name":"Arduino Mega 2560","slug":"arduino-mega-2560","avgRating":4.9,"soldQuantity":450,"minPrice":280000.00,"maxPrice":280000.00,"minDiscountPrice":259000.00,"maxDiscountPrice":259000.00,"defaultImageUrl":null,"totalStock":35,"hasDiscount":true,"categories":[{"id":1,"name":"Arduino & Modules","slug":"arduino-modules","imageUrl":"/images/ArduinoUnoR3.jpg","description":"Bo máº¡ch Arduino vÃ  cÃ¡c module má»Ÿ rá»™ng","createdAt":[2026,10,10,17,58,48,176768000],"updatedAt":[2026,10,10,17,58,48,176768000]}],"variants":[{"id":5,"productId":3,"productName":"Arduino Mega 2560","variantName":"Arduino Mega 2560 R3","description":"Arduino Mega 2560 dá»±a trÃªn chip ATmega2560, cÃ³ 54 digital I/O pins, 16 analog inputs, 4 UART. LÃ½ tÆ°á»Ÿng cho cÃ¡c dá»± Ã¡n phá»©c táº¡p cáº§n nhiá»u chÃ¢n I/O.","specifications":"Vi Ä‘iá»u khiá»ƒn: ATmega2560 | Digital I/O: 54 (15 PWM) | Analog Input: 16 | UART: 4 | Flash: 256KB | SRAM: 8KB | Clock: 16MHz","imageUrl":null,"price":280000.00,"discountPrice":259000.00,"inStock":35,"isAvailable":true,"createdAt":[2026,10,10,17,58,48,184340000],"updatedAt":[2026,10,10,17,58,48,184340000]}],"createdAt":[2026,10,10,17,58,48,178447000],"updatedAt":[2026,10,10,17,58,48,178447000]}]}', NULL);
INSERT INTO public.chat_tool_logs VALUES (3, '2026-10-11 00:05:22.99838', 0, NULL, '6ab8de1b-aefa-4cab-860b-c6be670ba998', 'SUCCESS', '{"threshold":10}', 'get_low_stock_products', '{"error":"Tá»« chá»‘i truy cáº­p: Chá»‰ Quáº£n trá»‹ viÃªn (Admin) má»›i cÃ³ quyá»n truy váº¥n sá»‘ liá»‡u tá»“n kho cáº£nh bÃ¡o."}', NULL);
INSERT INTO public.chat_tool_logs VALUES (4, '2026-10-11 00:12:08.518694', 14, NULL, '6dccf298-3472-4b43-a1e8-26d972a07804', 'SUCCESS', '{"query":"Arduino Uno R3"}', 'search_products', '{"query":"Arduino Uno R3","totalFound":1,"products":[{"id":1,"name":"Arduino Uno R3","slug":"arduino-uno-r3","avgRating":4.8,"soldQuantity":1250,"minPrice":120000.00,"maxPrice":180000.00,"minDiscountPrice":110000.00,"maxDiscountPrice":165000.00,"defaultImageUrl":null,"totalStock":165,"hasDiscount":true,"categories":[{"id":1,"name":"Arduino & Modules","slug":"arduino-modules","imageUrl":"/images/ArduinoUnoR3.jpg","description":"Bo máº¡ch Arduino vÃ  cÃ¡c module má»Ÿ rá»™ng","createdAt":[2026,10,10,17,58,48,176768000],"updatedAt":[2026,10,10,17,58,48,176768000]}],"variants":[{"id":1,"productId":1,"productName":"Arduino Uno R3","variantName":"Arduino Uno R3 - ChÃ­nh hÃ£ng","description":"Bo máº¡ch Arduino Uno R3 chÃ­nh hÃ£ng vá»›i chip ATmega328P, 14 digital I/O pins, 6 analog inputs, USB connection. PhÃ¹ há»£p cho ngÆ°á»i má»›i báº¯t Ä‘áº§u há»c láº­p trÃ¬nh nhÃºng.","specifications":"Vi Ä‘iá»u khiá»ƒn: ATmega328P | Äiá»‡n Ã¡p: 5V | Digital I/O: 14 (6 PWM) | Analog Input: 6 | Flash: 32KB | SRAM: 2KB | Clock: 16MHz","imageUrl":null,"price":180000.00,"discountPrice":165000.00,"inStock":45,"isAvailable":true,"createdAt":[2026,10,10,17,58,48,184340000],"updatedAt":[2026,10,10,17,58,48,184340000]},{"id":2,"productId":1,"productName":"Arduino Uno R3","variantName":"Arduino Uno R3 - Compatible","description":"Bo máº¡ch Arduino Uno R3 tÆ°Æ¡ng thÃ­ch, giÃ¡ ráº» hÆ¡n, phÃ¹ há»£p cho há»c táº­p vÃ  thá»­ nghiá»‡m.","specifications":"Vi Ä‘iá»u khiá»ƒn: ATmega328P | Äiá»‡n Ã¡p: 5V | Digital I/O: 14 (6 PWM) | Analog Input: 6 | Flash: 32KB | SRAM: 2KB | Clock: 16MHz","imageUrl":null,"price":120000.00,"discountPrice":110000.00,"inStock":120,"isAvailable":true,"createdAt":[2026,10,10,17,58,48,184340000],"updatedAt":[2026,10,10,17,58,48,184340000]}],"createdAt":[2026,10,10,17,58,48,178447000],"updatedAt":[2026,10,10,17,58,48,178447000]}]}', NULL);
INSERT INTO public.chat_tool_logs VALUES (5, '2026-10-11 00:12:32.995746', 2, NULL, '6dccf298-3472-4b43-a1e8-26d972a07804', 'SUCCESS', '{"productId":10001}', 'check_stock', '{"error":"KhÃ´ng tÃ¬m tháº¥y sáº£n pháº©m cÃ³ ID: 10001"}', NULL);
INSERT INTO public.chat_tool_logs VALUES (6, '2026-10-11 00:22:50.153623', 0, NULL, '6c552ce0-10ce-4a88-9905-9191abeb568e', 'SUCCESS', '{"threshold":10}', 'get_low_stock_products', '{"error":"Tá»« chá»‘i truy cáº­p: Chá»‰ Quáº£n trá»‹ viÃªn (Admin) má»›i cÃ³ quyá»n truy váº¥n sá»‘ liá»‡u tá»“n kho cáº£nh bÃ¡o."}', NULL);
INSERT INTO public.chat_tool_logs VALUES (7, '2026-10-11 00:37:10.266546', 9, NULL, 'a259831b-3efd-48eb-901f-882921850fde', 'SUCCESS', '{"threshold":10}', 'get_low_stock_products', '{"lowStockItems":[],"count":0,"threshold":10}', 4);
INSERT INTO public.chat_tool_logs VALUES (8, '2026-10-11 00:40:31.438465', 2, NULL, '9f6b9ae2-4460-4879-82c9-eed921711021', 'SUCCESS', '{"threshold":35}', 'get_low_stock_products', '{"lowStockItems":[{"price":1850000.00,"inStock":25,"variantId":9,"variantName":"Raspberry Pi 4B 4GB - Kit Ä‘áº§y Ä‘á»§","productName":"Raspberry Pi 4 Model B 4GB"},{"price":1250000.00,"inStock":30,"variantId":11,"variantName":"Raspberry Pi 3B+ - Kit cÆ¡ báº£n","productName":"Raspberry Pi 3 Model B+"},{"price":280000.00,"inStock":35,"variantId":5,"variantName":"Arduino Mega 2560 R3","productName":"Arduino Mega 2560"}],"count":3,"threshold":35}', 4);
INSERT INTO public.chat_tool_logs VALUES (9, '2026-10-11 00:41:50.625453', 4, NULL, '54b05f91-0a61-4dc6-8b18-5b0ad9edb715', 'SUCCESS', '{"limit":5}', 'get_top_selling_products', '{"topSellingProducts":[{"name":"TP4056 - Module sáº¡c pin Lithium","avgRating":4.8,"id":16,"slug":"tp4056-charger","soldQuantity":2450},{"name":"OLED 0.96\" I2C 128x64","avgRating":4.9,"id":23,"slug":"oled-096-i2c-128x64","soldQuantity":2340},{"name":"HC-SR04 - Cáº£m biáº¿n siÃªu Ã¢m","avgRating":4.6,"id":10,"slug":"hc-sr04-ultrasonic","soldQuantity":2230},{"name":"Äá»™ng cÆ¡ Servo SG90","avgRating":4.6,"id":26,"slug":"servo-sg90","soldQuantity":2120},{"name":"ESP32 DevKit V1","avgRating":4.9,"id":4,"slug":"esp32-devkit-v1","soldQuantity":2100}],"count":5}', 4);
INSERT INTO public.chat_tool_logs VALUES (10, '2026-10-11 00:49:31.268319', 0, NULL, '34766809-6c47-4dc0-87fa-ce86aedac5a9', 'SUCCESS', '{"threshold":10}', 'get_low_stock_products', '{"error":"Tá»« chá»‘i truy cáº­p: Chá»‰ Quáº£n trá»‹ viÃªn (Admin) má»›i cÃ³ quyá»n truy váº¥n sá»‘ liá»‡u tá»“n kho cáº£nh bÃ¡o."}', NULL);
INSERT INTO public.chat_tool_logs VALUES (11, '2026-10-11 00:54:09.272942', 1, NULL, '3eefffa1-a4f9-41e4-aeb0-26ad15c4c30c', 'SUCCESS', '{"threshold":10}', 'get_low_stock_products', '{"lowStockItems":[],"count":0,"threshold":10}', 4);
INSERT INTO public.chat_tool_logs VALUES (12, '2026-10-11 00:54:23.01489', 7, NULL, '7eff3ca7-571b-49fa-be9e-8fb67148f9e7', 'SUCCESS', '{"query":"Arduino Uno R3"}', 'search_products', '{"query":"Arduino Uno R3","totalFound":1,"products":[{"id":1,"name":"Arduino Uno R3","slug":"arduino-uno-r3","avgRating":4.8,"soldQuantity":1250,"minPrice":120000.00,"maxPrice":180000.00,"minDiscountPrice":110000.00,"maxDiscountPrice":165000.00,"defaultImageUrl":null,"totalStock":165,"hasDiscount":true,"categories":[{"id":1,"name":"Arduino & Modules","slug":"arduino-modules","imageUrl":"/images/ArduinoUnoR3.jpg","description":"Bo máº¡ch Arduino vÃ  cÃ¡c module má»Ÿ rá»™ng","createdAt":[2026,10,10,17,58,48,176768000],"updatedAt":[2026,10,10,17,58,48,176768000]}],"variants":[{"id":1,"productId":1,"productName":"Arduino Uno R3","variantName":"Arduino Uno R3 - ChÃ­nh hÃ£ng","description":"Bo máº¡ch Arduino Uno R3 chÃ­nh hÃ£ng vá»›i chip ATmega328P, 14 digital I/O pins, 6 analog inputs, USB connection. PhÃ¹ há»£p cho ngÆ°á»i má»›i báº¯t Ä‘áº§u há»c láº­p trÃ¬nh nhÃºng.","specifications":"Vi Ä‘iá»u khiá»ƒn: ATmega328P | Äiá»‡n Ã¡p: 5V | Digital I/O: 14 (6 PWM) | Analog Input: 6 | Flash: 32KB | SRAM: 2KB | Clock: 16MHz","imageUrl":null,"price":180000.00,"discountPrice":165000.00,"inStock":45,"isAvailable":true,"createdAt":[2026,10,10,17,58,48,184340000],"updatedAt":[2026,10,10,17,58,48,184340000]},{"id":2,"productId":1,"productName":"Arduino Uno R3","variantName":"Arduino Uno R3 - Compatible","description":"Bo máº¡ch Arduino Uno R3 tÆ°Æ¡ng thÃ­ch, giÃ¡ ráº» hÆ¡n, phÃ¹ há»£p cho há»c táº­p vÃ  thá»­ nghiá»‡m.","specifications":"Vi Ä‘iá»u khiá»ƒn: ATmega328P | Äiá»‡n Ã¡p: 5V | Digital I/O: 14 (6 PWM) | Analog Input: 6 | Flash: 32KB | SRAM: 2KB | Clock: 16MHz","imageUrl":null,"price":120000.00,"discountPrice":110000.00,"inStock":120,"isAvailable":true,"createdAt":[2026,10,10,17,58,48,184340000],"updatedAt":[2026,10,10,17,58,48,184340000]}],"createdAt":[2026,10,10,17,58,48,178447000],"updatedAt":[2026,10,10,17,58,48,178447000]}]}', 4);
INSERT INTO public.chat_tool_logs VALUES (13, '2026-10-11 04:11:59.058941', 4, NULL, '6c552ce0-10ce-4a88-9905-9191abeb568e', 'SUCCESS', '{"threshold":10}', 'get_low_stock_products', '{"lowStockItems":[],"count":0,"threshold":10}', 4);
INSERT INTO public.chat_tool_logs VALUES (14, '2026-10-11 04:30:17.786293', 20, NULL, '6c552ce0-10ce-4a88-9905-9191abeb568e', 'SUCCESS', '{"query":"ESP32"}', 'search_products', '{"query":"ESP32","totalFound":1,"products":[{"id":4,"name":"ESP32 DevKit V1","slug":"esp32-devkit-v1","avgRating":4.9,"soldQuantity":2100,"minPrice":95000.00,"maxPrice":105000.00,"minDiscountPrice":89000.00,"maxDiscountPrice":98000.00,"defaultImageUrl":null,"totalStock":330,"hasDiscount":true,"categories":[{"id":1,"name":"Arduino & Modules","slug":"arduino-modules","imageUrl":"/images/ArduinoUnoR3.jpg","description":"Bo máº¡ch Arduino vÃ  cÃ¡c module má»Ÿ rá»™ng","createdAt":[2026,10,10,17,58,48,176768000],"updatedAt":[2026,10,10,17,58,48,176768000]}],"variants":[{"id":6,"productId":4,"productName":"ESP32 DevKit V1","variantName":"ESP32 DevKit V1 - 30 pins","description":"ESP32 DevKit V1 phiÃªn báº£n 30 chÃ¢n, WiFi + Bluetooth tÃ­ch há»£p, dual-core 240MHz. HoÃ n háº£o cho IoT projects.","specifications":"Chip: ESP32-WROOM-32 | CPU: Dual-core 240MHz | SRAM: 520KB | Flash: 4MB | WiFi: 802.11 b/g/n | Bluetooth: v4.2 | GPIO: 30 pins","imageUrl":null,"price":95000.00,"discountPrice":89000.00,"inStock":180,"isAvailable":true,"createdAt":[2026,10,10,17,58,48,184340000],"updatedAt":[2026,10,10,17,58,48,184340000]},{"id":7,"productId":4,"productName":"ESP32 DevKit V1","variantName":"ESP32 DevKit V1 - 38 pins","description":"ESP32 DevKit V1 phiÃªn báº£n 38 chÃ¢n, nhiá»u GPIO hÆ¡n cho cÃ¡c dá»± Ã¡n phá»©c táº¡p.","specifications":"Chip: ESP32-WROOM-32 | CPU: Dual-core 240MHz | SRAM: 520KB | Flash: 4MB | WiFi: 802.11 b/g/n | Bluetooth: v4.2 | GPIO: 38 pins","imageUrl":null,"price":105000.00,"discountPrice":98000.00,"inStock":150,"isAvailable":true,"createdAt":[2026,10,10,17,58,48,184340000],"updatedAt":[2026,10,10,17,58,48,184340000]}],"createdAt":[2026,10,10,17,58,48,178447000],"updatedAt":[2026,10,10,17,58,48,178447000]}]}', 4);
INSERT INTO public.chat_tool_logs VALUES (15, '2026-10-12 00:46:23.47937', 19, NULL, '7f7dd076-f943-46f3-abb3-02dd42a086a6', 'SUCCESS', '{"endDate":"2026-10-31","period":"month","startDate":"2026-10-01"}', 'get_revenue_report', '{"summary":"Doanh thu tá»« 2026-10-01 Ä‘áº¿n 2026-10-31 Ä‘áº¡t 0 VNÄ vá»›i 0 Ä‘Æ¡n hoÃ n thÃ nh (trÃªn tá»•ng sá»‘ 0 Ä‘Æ¡n Ä‘áº·t).","period":"month","formattedRevenue":"0 VNÄ","endDate":"2026-10-31","deliveredOrders":0,"totalRevenue":0,"totalOrders":0,"startDate":"2026-10-01"}', 4);
INSERT INTO public.chat_tool_logs VALUES (16, '2026-10-12 00:58:51.859422', 6, NULL, '10be8541-b2c6-42f5-a53b-4a640fea4516', 'SUCCESS', '{"productId":1001}', 'check_stock', '{"error":"KhÃ´ng tÃ¬m tháº¥y sáº£n pháº©m cÃ³ ID: 1001"}', NULL);
INSERT INTO public.chat_tool_logs VALUES (17, '2026-10-12 02:06:59.115281', 6, NULL, 'e5748f0e-03cb-4fd2-9b33-f774bd249306', 'SUCCESS', '{"query":"Arduino Uno R3"}', 'search_products', '{"query":"Arduino Uno R3","totalFound":1,"products":[{"id":1,"name":"Arduino Uno R3","slug":"arduino-uno-r3","avgRating":4.8,"soldQuantity":1250,"minPrice":120000.00,"maxPrice":180000.00,"minDiscountPrice":110000.00,"maxDiscountPrice":165000.00,"defaultImageUrl":null,"totalStock":165,"hasDiscount":true,"categories":[{"id":1,"name":"Arduino & Modules","slug":"arduino-modules","imageUrl":"/images/ArduinoUnoR3.jpg","description":"Bo máº¡ch Arduino vÃ  cÃ¡c module má»Ÿ rá»™ng","createdAt":[2026,10,10,17,58,48,176768000],"updatedAt":[2026,10,10,17,58,48,176768000]}],"variants":[{"id":1,"productId":1,"productName":"Arduino Uno R3","variantName":"Arduino Uno R3 - ChÃ­nh hÃ£ng","description":"Bo máº¡ch Arduino Uno R3 chÃ­nh hÃ£ng vá»›i chip ATmega328P, 14 digital I/O pins, 6 analog inputs, USB connection. PhÃ¹ há»£p cho ngÆ°á»i má»›i báº¯t Ä‘áº§u há»c láº­p trÃ¬nh nhÃºng.","specifications":"Vi Ä‘iá»u khiá»ƒn: ATmega328P | Äiá»‡n Ã¡p: 5V | Digital I/O: 14 (6 PWM) | Analog Input: 6 | Flash: 32KB | SRAM: 2KB | Clock: 16MHz","imageUrl":null,"price":180000.00,"discountPrice":165000.00,"inStock":45,"isAvailable":true,"createdAt":[2026,10,10,17,58,48,184340000],"updatedAt":[2026,10,10,17,58,48,184340000]},{"id":2,"productId":1,"productName":"Arduino Uno R3","variantName":"Arduino Uno R3 - Compatible","description":"Bo máº¡ch Arduino Uno R3 tÆ°Æ¡ng thÃ­ch, giÃ¡ ráº» hÆ¡n, phÃ¹ há»£p cho há»c táº­p vÃ  thá»­ nghiá»‡m.","specifications":"Vi Ä‘iá»u khiá»ƒn: ATmega328P | Äiá»‡n Ã¡p: 5V | Digital I/O: 14 (6 PWM) | Analog Input: 6 | Flash: 32KB | SRAM: 2KB | Clock: 16MHz","imageUrl":null,"price":120000.00,"discountPrice":110000.00,"inStock":120,"isAvailable":true,"createdAt":[2026,10,10,17,58,48,184340000],"updatedAt":[2026,10,10,17,58,48,184340000]}],"createdAt":[2026,10,10,17,58,48,178447000],"updatedAt":[2026,10,10,17,58,48,178447000]}]}', NULL);


--
-- Data for Name: inventory; Type: TABLE DATA; Schema: public; Owner: -
--



--
-- Data for Name: notifications; Type: TABLE DATA; Schema: public; Owner: -
--



--
-- Data for Name: order_items; Type: TABLE DATA; Schema: public; Owner: -
--



--
-- Data for Name: orders; Type: TABLE DATA; Schema: public; Owner: -
--



--
-- Data for Name: payments; Type: TABLE DATA; Schema: public; Owner: -
--



--
-- Data for Name: product_categories; Type: TABLE DATA; Schema: public; Owner: -
--

INSERT INTO public.product_categories VALUES (1, 1);
INSERT INTO public.product_categories VALUES (2, 1);
INSERT INTO public.product_categories VALUES (3, 1);
INSERT INTO public.product_categories VALUES (4, 1);
INSERT INTO public.product_categories VALUES (5, 1);
INSERT INTO public.product_categories VALUES (6, 2);
INSERT INTO public.product_categories VALUES (7, 2);
INSERT INTO public.product_categories VALUES (8, 2);
INSERT INTO public.product_categories VALUES (9, 4);
INSERT INTO public.product_categories VALUES (10, 4);
INSERT INTO public.product_categories VALUES (11, 4);
INSERT INTO public.product_categories VALUES (12, 4);
INSERT INTO public.product_categories VALUES (13, 4);
INSERT INTO public.product_categories VALUES (14, 5);
INSERT INTO public.product_categories VALUES (15, 5);
INSERT INTO public.product_categories VALUES (16, 5);
INSERT INTO public.product_categories VALUES (17, 5);
INSERT INTO public.product_categories VALUES (18, 6);
INSERT INTO public.product_categories VALUES (19, 6);
INSERT INTO public.product_categories VALUES (20, 6);
INSERT INTO public.product_categories VALUES (21, 6);
INSERT INTO public.product_categories VALUES (22, 7);
INSERT INTO public.product_categories VALUES (23, 7);
INSERT INTO public.product_categories VALUES (24, 7);
INSERT INTO public.product_categories VALUES (25, 8);
INSERT INTO public.product_categories VALUES (26, 8);
INSERT INTO public.product_categories VALUES (27, 8);
INSERT INTO public.product_categories VALUES (28, 8);


--
-- Data for Name: product_variants; Type: TABLE DATA; Schema: public; Owner: -
--

INSERT INTO public.product_variants VALUES (1, 1, 'Arduino Uno R3 - ChÃ­nh hÃ£ng', 'Bo máº¡ch Arduino Uno R3 chÃ­nh hÃ£ng vá»›i chip ATmega328P, 14 digital I/O pins, 6 analog inputs, USB connection. PhÃ¹ há»£p cho ngÆ°á»i má»›i báº¯t Ä‘áº§u há»c láº­p trÃ¬nh nhÃºng.', 'Vi Ä‘iá»u khiá»ƒn: ATmega328P | Äiá»‡n Ã¡p: 5V | Digital I/O: 14 (6 PWM) | Analog Input: 6 | Flash: 32KB | SRAM: 2KB | Clock: 16MHz', NULL, 180000.00, 165000.00, 45, true, '2026-10-10 17:58:48.18434', '2026-10-10 17:58:48.18434');
INSERT INTO public.product_variants VALUES (2, 1, 'Arduino Uno R3 - Compatible', 'Bo máº¡ch Arduino Uno R3 tÆ°Æ¡ng thÃ­ch, giÃ¡ ráº» hÆ¡n, phÃ¹ há»£p cho há»c táº­p vÃ  thá»­ nghiá»‡m.', 'Vi Ä‘iá»u khiá»ƒn: ATmega328P | Äiá»‡n Ã¡p: 5V | Digital I/O: 14 (6 PWM) | Analog Input: 6 | Flash: 32KB | SRAM: 2KB | Clock: 16MHz', NULL, 120000.00, 110000.00, 120, true, '2026-10-10 17:58:48.18434', '2026-10-10 17:58:48.18434');
INSERT INTO public.product_variants VALUES (3, 2, 'Arduino Nano V3 CH340', 'Arduino Nano V3 sá»­ dá»¥ng chip CH340, kÃ­ch thÆ°á»›c nhá» gá»n phÃ¹ há»£p cho cÃ¡c dá»± Ã¡n cÃ³ khÃ´ng gian háº¡n cháº¿.', 'Vi Ä‘iá»u khiá»ƒn: ATmega328P | USB: CH340 | Äiá»‡n Ã¡p: 5V | Digital I/O: 14 | Analog Input: 8 | Flash: 32KB', NULL, 65000.00, 59000.00, 200, true, '2026-10-10 17:58:48.18434', '2026-10-10 17:58:48.18434');
INSERT INTO public.product_variants VALUES (4, 2, 'Arduino Nano V3 FTDI', 'Arduino Nano V3 sá»­ dá»¥ng chip FTDI chÃ­nh hÃ£ng, á»•n Ä‘á»‹nh hÆ¡n trong giao tiáº¿p serial.', 'Vi Ä‘iá»u khiá»ƒn: ATmega328P | USB: FTDI | Äiá»‡n Ã¡p: 5V | Digital I/O: 14 | Analog Input: 8 | Flash: 32KB', NULL, 85000.00, NULL, 80, true, '2026-10-10 17:58:48.18434', '2026-10-10 17:58:48.18434');
INSERT INTO public.product_variants VALUES (5, 3, 'Arduino Mega 2560 R3', 'Arduino Mega 2560 dá»±a trÃªn chip ATmega2560, cÃ³ 54 digital I/O pins, 16 analog inputs, 4 UART. LÃ½ tÆ°á»Ÿng cho cÃ¡c dá»± Ã¡n phá»©c táº¡p cáº§n nhiá»u chÃ¢n I/O.', 'Vi Ä‘iá»u khiá»ƒn: ATmega2560 | Digital I/O: 54 (15 PWM) | Analog Input: 16 | UART: 4 | Flash: 256KB | SRAM: 8KB | Clock: 16MHz', NULL, 280000.00, 259000.00, 35, true, '2026-10-10 17:58:48.18434', '2026-10-10 17:58:48.18434');
INSERT INTO public.product_variants VALUES (6, 4, 'ESP32 DevKit V1 - 30 pins', 'ESP32 DevKit V1 phiÃªn báº£n 30 chÃ¢n, WiFi + Bluetooth tÃ­ch há»£p, dual-core 240MHz. HoÃ n háº£o cho IoT projects.', 'Chip: ESP32-WROOM-32 | CPU: Dual-core 240MHz | SRAM: 520KB | Flash: 4MB | WiFi: 802.11 b/g/n | Bluetooth: v4.2 | GPIO: 30 pins', NULL, 95000.00, 89000.00, 180, true, '2026-10-10 17:58:48.18434', '2026-10-10 17:58:48.18434');
INSERT INTO public.product_variants VALUES (7, 4, 'ESP32 DevKit V1 - 38 pins', 'ESP32 DevKit V1 phiÃªn báº£n 38 chÃ¢n, nhiá»u GPIO hÆ¡n cho cÃ¡c dá»± Ã¡n phá»©c táº¡p.', 'Chip: ESP32-WROOM-32 | CPU: Dual-core 240MHz | SRAM: 520KB | Flash: 4MB | WiFi: 802.11 b/g/n | Bluetooth: v4.2 | GPIO: 38 pins', NULL, 105000.00, 98000.00, 150, true, '2026-10-10 17:58:48.18434', '2026-10-10 17:58:48.18434');
INSERT INTO public.product_variants VALUES (8, 5, 'NodeMCU ESP8266 V3', 'NodeMCU ESP8266 lÃ  board phÃ¡t triá»ƒn WiFi giÃ¡ ráº», dá»… láº­p trÃ¬nh vá»›i Arduino IDE hoáº·c Lua. PhÃ¹ há»£p cho cÃ¡c dá»± Ã¡n IoT Ä‘Æ¡n giáº£n.', 'Chip: ESP8266 | CPU: 80/160MHz | Flash: 4MB | WiFi: 802.11 b/g/n | GPIO: 17 | ADC: 1 (10-bit) | Äiá»‡n Ã¡p: 3.3V', NULL, 75000.00, 69000.00, 250, true, '2026-10-10 17:58:48.18434', '2026-10-10 17:58:48.18434');
INSERT INTO public.product_variants VALUES (9, 6, 'Raspberry Pi 4B 4GB - Kit Ä‘áº§y Ä‘á»§', 'Kit Ä‘áº§y Ä‘á»§ bao gá»“m board Raspberry Pi 4B 4GB RAM, nguá»“n, vá» case, tháº» nhá»›, quáº¡t táº£n nhiá»‡t.', 'CPU: Quad-core ARM Cortex-A72 1.5GHz | RAM: 4GB | Display: Dual 4K | Ethernet: Gigabit | USB: 2x USB 3.0, 2x USB 2.0 | WiFi: 5GHz | Bluetooth: 5.0', NULL, 1850000.00, 1750000.00, 25, true, '2026-10-10 17:58:48.18434', '2026-10-10 17:58:48.18434');
INSERT INTO public.product_variants VALUES (10, 6, 'Raspberry Pi 4B 4GB - Board only', 'Board Raspberry Pi 4 Model B 4GB RAM, khÃ´ng bao gá»“m phá»¥ kiá»‡n.', 'CPU: Quad-core ARM Cortex-A72 1.5GHz | RAM: 4GB | Display: Dual 4K | Ethernet: Gigabit | USB: 2x USB 3.0, 2x USB 2.0 | WiFi: 5GHz | Bluetooth: 5.0', NULL, 1450000.00, 1380000.00, 40, true, '2026-10-10 17:58:48.18434', '2026-10-10 17:58:48.18434');
INSERT INTO public.product_variants VALUES (11, 7, 'Raspberry Pi 3B+ - Kit cÆ¡ báº£n', 'Kit cÆ¡ báº£n bao gá»“m board Raspberry Pi 3 B+, nguá»“n vÃ  vá» case.', 'CPU: Quad-core 1.4GHz | RAM: 1GB | WiFi: Dual-band | Bluetooth: 4.2 | Ethernet: Gigabit (qua USB 2.0)', NULL, 1250000.00, 1180000.00, 30, true, '2026-10-10 17:58:48.18434', '2026-10-10 17:58:48.18434');
INSERT INTO public.product_variants VALUES (12, 7, 'Raspberry Pi 3B+ - Board only', 'Board Raspberry Pi 3 Model B+ vá»›i WiFi dual-band, khÃ´ng phá»¥ kiá»‡n.', 'CPU: Quad-core 1.4GHz | RAM: 1GB | WiFi: Dual-band | Bluetooth: 4.2 | Ethernet: Gigabit (qua USB 2.0)', NULL, 980000.00, NULL, 45, true, '2026-10-10 17:58:48.18434', '2026-10-10 17:58:48.18434');
INSERT INTO public.product_variants VALUES (13, 8, 'Raspberry Pi Zero W', 'Raspberry Pi Zero W kÃ­ch thÆ°á»›c cá»±c nhá» vá»›i WiFi vÃ  Bluetooth tÃ­ch há»£p. LÃ½ tÆ°á»Ÿng cho cÃ¡c dá»± Ã¡n yÃªu cáº§u kÃ­ch thÆ°á»›c nhá».', 'CPU: 1GHz single-core | RAM: 512MB | WiFi: 802.11n | Bluetooth: 4.1 | GPIO: 40 pins | Video: Mini HDMI', NULL, 320000.00, 299000.00, 60, true, '2026-10-10 17:58:48.18434', '2026-10-10 17:58:48.18434');
INSERT INTO public.product_variants VALUES (14, 9, 'DHT22 - Module cÃ³ board', 'DHT22 module cÃ³ board máº¡ch, dá»… káº¿t ná»‘i, Ä‘o nhiá»‡t Ä‘á»™ vÃ  Ä‘á»™ áº©m chÃ­nh xÃ¡c cao.', 'Nhiá»‡t Ä‘á»™: -40~80Â°C (Â±0.5Â°C) | Äá»™ áº©m: 0-100% RH (Â±2-5%) | Giao tiáº¿p: Digital | Äiá»‡n Ã¡p: 3.3-5V', NULL, 85000.00, 79000.00, 150, true, '2026-10-10 17:58:48.18434', '2026-10-10 17:58:48.18434');
INSERT INTO public.product_variants VALUES (15, 9, 'DHT22 - Sensor only', 'DHT22 cáº£m biáº¿n rá»i, cáº§n tá»± káº¿t ná»‘i vá»›i Ä‘iá»‡n trá»Ÿ pull-up.', 'Nhiá»‡t Ä‘á»™: -40~80Â°C (Â±0.5Â°C) | Äá»™ áº©m: 0-100% RH (Â±2-5%) | Giao tiáº¿p: Digital | Äiá»‡n Ã¡p: 3.3-5V', NULL, 65000.00, NULL, 200, true, '2026-10-10 17:58:48.18434', '2026-10-10 17:58:48.18434');
INSERT INTO public.product_variants VALUES (16, 10, 'HC-SR04 Ultrasonic', 'HC-SR04 sá»­ dá»¥ng sÃ³ng siÃªu Ã¢m Ä‘á»ƒ Ä‘o khoáº£ng cÃ¡ch tá»« 2cm Ä‘áº¿n 400cm vá»›i Ä‘á»™ chÃ­nh xÃ¡c 3mm.', 'Khoáº£ng cÃ¡ch: 2-400cm | Äá»™ chÃ­nh xÃ¡c: 3mm | Táº§n sá»‘: 40KHz | Äiá»‡n Ã¡p: 5V | GÃ³c Ä‘o: 15Â°', NULL, 35000.00, 32000.00, 300, true, '2026-10-10 17:58:48.18434', '2026-10-10 17:58:48.18434');
INSERT INTO public.product_variants VALUES (17, 11, 'BMP280 Module I2C', 'BMP280 Ä‘o Ã¡p suáº¥t khÃ­ quyá»ƒn vÃ  nhiá»‡t Ä‘á»™, giao tiáº¿p I2C, tiÃªu thá»¥ Ä‘iá»‡n nÄƒng cá»±c tháº¥p.', 'Ãp suáº¥t: 300-1100 hPa | Nhiá»‡t Ä‘á»™: -40~+85Â°C | Giao tiáº¿p: I2C/SPI | Äiá»‡n Ã¡p: 1.8-3.6V | Äá»™ phÃ¢n giáº£i: 0.01 hPa', NULL, 55000.00, 49000.00, 100, true, '2026-10-10 17:58:48.18434', '2026-10-10 17:58:48.18434');
INSERT INTO public.product_variants VALUES (18, 12, 'MPU6050 GY-521', 'MPU6050 tÃ­ch há»£p 3-axis gyroscope vÃ  3-axis accelerometer, giao tiáº¿p I2C. Sá»­ dá»¥ng trong drone, robot tá»± cÃ¢n báº±ng.', 'Gyroscope: 3-axis (Â±250/500/1000/2000Â°/s) | Accelerometer: 3-axis (Â±2/4/8/16g) | Giao tiáº¿p: I2C | Äiá»‡n Ã¡p: 3-5V | ADC: 16-bit', NULL, 65000.00, 59000.00, 180, true, '2026-10-10 17:58:48.18434', '2026-10-10 17:58:48.18434');
INSERT INTO public.product_variants VALUES (19, 13, 'MQ-2 Gas Sensor Module', 'MQ-2 phÃ¡t hiá»‡n cÃ¡c loáº¡i khÃ­ dá»… chÃ¡y nhÆ° LPG, propane, methane, hydrogen. Äáº§u ra analog vÃ  digital.', 'KhÃ­ phÃ¡t hiá»‡n: LPG, propane, methane, H2, alcohol | Äáº§u ra: Analog + Digital | Äiá»‡n Ã¡p: 5V | Thá»i gian Ä‘Ã¡p á»©ng: <10s', NULL, 45000.00, 39000.00, 120, true, '2026-10-10 17:58:48.18434', '2026-10-10 17:58:48.18434');
INSERT INTO public.product_variants VALUES (20, 14, 'LM2596 Buck Converter', 'LM2596 step-down converter vá»›i dÃ²ng 3A, Ä‘iá»‡n Ã¡p Ä‘áº§u vÃ o 4-40V, Ä‘áº§u ra Ä‘iá»u chá»‰nh 1.25-37V.', 'DÃ²ng ra: 3A | Vin: 4-40V | Vout: 1.25-37V | Hiá»‡u suáº¥t: 92% | Táº§n sá»‘: 150KHz', NULL, 35000.00, 32000.00, 200, true, '2026-10-10 17:58:48.18434', '2026-10-10 17:58:48.18434');
INSERT INTO public.product_variants VALUES (21, 15, 'MT3608 Boost Module', 'MT3608 step-up converter tÄƒng Ä‘iá»‡n Ã¡p tá»« 2-24V lÃªn 5-28V vá»›i dÃ²ng 2A.', 'DÃ²ng ra: 2A | Vin: 2-24V | Vout: 5-28V | Hiá»‡u suáº¥t: 93% | Táº§n sá»‘: 1.2MHz', NULL, 25000.00, 22000.00, 180, true, '2026-10-10 17:58:48.18434', '2026-10-10 17:58:48.18434');
INSERT INTO public.product_variants VALUES (22, 16, 'TP4056 vá»›i báº£o vá»‡', 'TP4056 sáº¡c pin lithium 1S vá»›i dÃ²ng sáº¡c 1A, cÃ³ máº¡ch báº£o vá»‡ quÃ¡ sáº¡c, quÃ¡ phÃ³ng, ngáº¯n máº¡ch.', 'DÃ²ng sáº¡c: 1A | Vin: 5V (Micro USB) | Pin: Li-ion/Li-Po 1S | Báº£o vá»‡: QuÃ¡ sáº¡c, quÃ¡ phÃ³ng, ngáº¯n máº¡ch', NULL, 18000.00, 15000.00, 400, true, '2026-10-10 17:58:48.18434', '2026-10-10 17:58:48.18434');
INSERT INTO public.product_variants VALUES (23, 16, 'TP4056 khÃ´ng báº£o vá»‡', 'TP4056 sáº¡c pin lithium 1S vá»›i dÃ²ng sáº¡c 1A, khÃ´ng cÃ³ máº¡ch báº£o vá»‡.', 'DÃ²ng sáº¡c: 1A | Vin: 5V (Micro USB) | Pin: Li-ion/Li-Po 1S | Báº£o vá»‡: KhÃ´ng', NULL, 12000.00, NULL, 350, true, '2026-10-10 17:58:48.18434', '2026-10-10 17:58:48.18434');
INSERT INTO public.product_variants VALUES (24, 17, 'AMS1117-3.3V Module', 'AMS1117-3.3V cung cáº¥p nguá»“n á»•n Ä‘á»‹nh 3.3V vá»›i dÃ²ng tá»‘i Ä‘a 1A.', 'Vout: 3.3V | DÃ²ng ra: 1A | Vin: 4.5-12V | Loáº¡i: Linear regulator | Dropout: 1.3V', NULL, 15000.00, 12000.00, 250, true, '2026-10-10 17:58:48.18434', '2026-10-10 17:58:48.18434');
INSERT INTO public.product_variants VALUES (25, 18, 'NRF24L01+ cÆ¡ báº£n', 'NRF24L01+ module RF cÆ¡ báº£n, anten tÃ­ch há»£p trÃªn PCB, khoáº£ng cÃ¡ch ~100m.', 'Táº§n sá»‘: 2.4GHz | Tá»‘c Ä‘á»™: 250Kbps-2Mbps | Khoáº£ng cÃ¡ch: ~100m | Giao tiáº¿p: SPI | Äiá»‡n Ã¡p: 3.3V', NULL, 35000.00, 32000.00, 150, true, '2026-10-10 17:58:48.18434', '2026-10-10 17:58:48.18434');
INSERT INTO public.product_variants VALUES (26, 18, 'NRF24L01+ cÃ³ anten', 'NRF24L01+ module RF vá»›i anten ngoÃ i, khoáº£ng cÃ¡ch truyá»n xa hÆ¡n ~1000m.', 'Táº§n sá»‘: 2.4GHz | Tá»‘c Ä‘á»™: 250Kbps-2Mbps | Khoáº£ng cÃ¡ch: ~1000m | Giao tiáº¿p: SPI | Äiá»‡n Ã¡p: 3.3V | Anten: NgoÃ i', NULL, 55000.00, 49000.00, 100, true, '2026-10-10 17:58:48.18434', '2026-10-10 17:58:48.18434');
INSERT INTO public.product_variants VALUES (27, 19, 'HC-05 Master/Slave', 'HC-05 lÃ  module Bluetooth 2.0 cÃ³ thá»ƒ cáº¥u hÃ¬nh Master/Slave, giao tiáº¿p UART Ä‘Æ¡n giáº£n.', 'Bluetooth: 2.0 | Baud rate: 9600-115200 | Khoáº£ng cÃ¡ch: 10m | Giao tiáº¿p: UART | Äiá»‡n Ã¡p: 3.3-6V | Cháº¿ Ä‘á»™: Master/Slave', NULL, 95000.00, 89000.00, 200, true, '2026-10-10 17:58:48.18434', '2026-10-10 17:58:48.18434');
INSERT INTO public.product_variants VALUES (28, 20, 'SIM800L GSM Module', 'SIM800L há»— trá»£ GSM/GPRS quad-band, gá»i Ä‘iá»‡n thoáº¡i, gá»­i/nháº­n SMS, GPRS data.', 'Máº¡ng: GSM/GPRS Quad-band | Giao tiáº¿p: UART | SIM: Mini SIM | Äiá»‡n Ã¡p: 3.4-4.4V | DÃ²ng: 2A peak', NULL, 180000.00, 165000.00, 60, true, '2026-10-10 17:58:48.18434', '2026-10-10 17:58:48.18434');
INSERT INTO public.product_variants VALUES (29, 21, 'LoRa 433MHz SX1278', 'LoRa SX1278 hoáº¡t Ä‘á»™ng á»Ÿ táº§n sá»‘ 433MHz, khoáº£ng cÃ¡ch truyá»n 3-5km, tiÃªu thá»¥ Ä‘iá»‡n nÄƒng cá»±c tháº¥p.', 'Táº§n sá»‘: 433MHz | Khoáº£ng cÃ¡ch: 3-5km | Tá»‘c Ä‘á»™: 0.3-19.2Kbps | Giao tiáº¿p: SPI | Äiá»‡n Ã¡p: 3.3V | CÃ´ng suáº¥t: 20dBm', NULL, 135000.00, 125000.00, 80, true, '2026-10-10 17:58:48.18434', '2026-10-10 17:58:48.18434');
INSERT INTO public.product_variants VALUES (30, 22, 'LCD 1602 I2C xanh lÃ¡', 'LCD 1602 ná»n xanh lÃ¡ chá»¯ tráº¯ng vá»›i module I2C, chá»‰ cáº§n 2 chÃ¢n SDA/SCL.', 'Hiá»ƒn thá»‹: 16x2 kÃ½ tá»± | Giao tiáº¿p: I2C | Ná»n: Xanh lÃ¡ | Chá»¯: Tráº¯ng | Äiá»‡n Ã¡p: 5V | Äiá»u chá»‰nh Ä‘á»™ tÆ°Æ¡ng pháº£n: CÃ³', NULL, 65000.00, 59000.00, 180, true, '2026-10-10 17:58:48.18434', '2026-10-10 17:58:48.18434');
INSERT INTO public.product_variants VALUES (31, 22, 'LCD 1602 I2C xanh dÆ°Æ¡ng', 'LCD 1602 ná»n xanh dÆ°Æ¡ng chá»¯ tráº¯ng vá»›i module I2C, chá»‰ cáº§n 2 chÃ¢n SDA/SCL.', 'Hiá»ƒn thá»‹: 16x2 kÃ½ tá»± | Giao tiáº¿p: I2C | Ná»n: Xanh dÆ°Æ¡ng | Chá»¯: Tráº¯ng | Äiá»‡n Ã¡p: 5V | Äiá»u chá»‰nh Ä‘á»™ tÆ°Æ¡ng pháº£n: CÃ³', NULL, 65000.00, 59000.00, 150, true, '2026-10-10 17:58:48.18434', '2026-10-10 17:58:48.18434');
INSERT INTO public.product_variants VALUES (32, 23, 'OLED 0.96" mÃ u tráº¯ng', 'OLED 0.96 inch mÃ u tráº¯ng, Ä‘á»™ phÃ¢n giáº£i 128x64 pixels, gÃ³c nhÃ¬n rá»™ng, Ä‘á»™ tÆ°Æ¡ng pháº£n cao.', 'KÃ­ch thÆ°á»›c: 0.96 inch | PhÃ¢n giáº£i: 128x64 | MÃ u: Tráº¯ng | Giao tiáº¿p: I2C | Äiá»‡n Ã¡p: 3.3-5V | Driver: SSD1306', NULL, 85000.00, 79000.00, 250, true, '2026-10-10 17:58:48.18434', '2026-10-10 17:58:48.18434');
INSERT INTO public.product_variants VALUES (33, 23, 'OLED 0.96" mÃ u xanh', 'OLED 0.96 inch mÃ u xanh dÆ°Æ¡ng, Ä‘á»™ phÃ¢n giáº£i 128x64 pixels, gÃ³c nhÃ¬n rá»™ng.', 'KÃ­ch thÆ°á»›c: 0.96 inch | PhÃ¢n giáº£i: 128x64 | MÃ u: Xanh dÆ°Æ¡ng | Giao tiáº¿p: I2C | Äiá»‡n Ã¡p: 3.3-5V | Driver: SSD1306', NULL, 85000.00, 79000.00, 200, true, '2026-10-10 17:58:48.18434', '2026-10-10 17:58:48.18434');
INSERT INTO public.product_variants VALUES (34, 24, 'TFT 2.4" cáº£m á»©ng', 'TFT 2.4 inch mÃ u 262K vá»›i Ä‘á»™ phÃ¢n giáº£i 240x320, tÃ­ch há»£p cáº£m á»©ng Ä‘iá»‡n trá»Ÿ.', 'KÃ­ch thÆ°á»›c: 2.4 inch | PhÃ¢n giáº£i: 240x320 | MÃ u: 262K | Cáº£m á»©ng: Äiá»‡n trá»Ÿ | Giao tiáº¿p: SPI | Driver: ILI9341', NULL, 185000.00, 169000.00, 70, true, '2026-10-10 17:58:48.18434', '2026-10-10 17:58:48.18434');
INSERT INTO public.product_variants VALUES (35, 25, 'L298N Motor Driver', 'L298N cÃ³ thá»ƒ Ä‘iá»u khiá»ƒn 2 Ä‘á»™ng cÆ¡ DC hoáº·c 1 Ä‘á»™ng cÆ¡ stepper, dÃ²ng tá»‘i Ä‘a 2A/motor.', 'DÃ²ng: 2A/motor | Äiá»‡n Ã¡p motor: 5-35V | Äiá»u khiá»ƒn: 2 DC hoáº·c 1 Stepper | á»”n Ã¡p 5V tÃ­ch há»£p: CÃ³ | Logic: 5V', NULL, 55000.00, 49000.00, 180, true, '2026-10-10 17:58:48.18434', '2026-10-10 17:58:48.18434');
INSERT INTO public.product_variants VALUES (36, 26, 'Servo SG90 9g', 'SG90 lÃ  servo nhá» gá»n trá»ng lÆ°á»£ng 9g, gÃ³c quay 180Â°, Ä‘iá»u khiá»ƒn PWM.', 'Trá»ng lÆ°á»£ng: 9g | GÃ³c quay: 180Â° | Momen xoáº¯n: 1.8kg.cm | Äiá»u khiá»ƒn: PWM | Äiá»‡n Ã¡p: 4.8-6V | Tá»‘c Ä‘á»™: 0.1s/60Â°', NULL, 35000.00, 32000.00, 300, true, '2026-10-10 17:58:48.18434', '2026-10-10 17:58:48.18434');
INSERT INTO public.product_variants VALUES (37, 27, 'DC Motor 12V 100RPM', 'Äá»™ng cÆ¡ DC 12V vá»›i há»™p sá»‘ giáº£m tá»‘c tÃ­ch há»£p, tá»‘c Ä‘á»™ 100RPM, cÃ³ encoder Ä‘o tá»‘c Ä‘á»™.', 'Äiá»‡n Ã¡p: 12V | Tá»‘c Ä‘á»™: 100RPM | Encoder: CÃ³ | Momen xoáº¯n: Cao | Trá»¥c: 6mm | á»¨ng dá»¥ng: Robot di Ä‘á»™ng', NULL, 125000.00, 115000.00, 90, true, '2026-10-10 17:58:48.18434', '2026-10-10 17:58:48.18434');
INSERT INTO public.product_variants VALUES (38, 28, 'Stepper 28BYJ-48 + ULN2003', '28BYJ-48 lÃ  Ä‘á»™ng cÆ¡ bÆ°á»›c 5V giáº£m tá»‘c 1/64, Ä‘i kÃ¨m driver ULN2003.', 'Äiá»‡n Ã¡p: 5V | Giáº£m tá»‘c: 1/64 | GÃ³c bÆ°á»›c: 5.625Â° | Driver: ULN2003 | Sá»‘ pha: 4 | TiÃªu thá»¥: ~240mA', NULL, 65000.00, 59000.00, 120, true, '2026-10-10 17:58:48.18434', '2026-10-10 17:58:48.18434');


--
-- Data for Name: products; Type: TABLE DATA; Schema: public; Owner: -
--

INSERT INTO public.products VALUES (1, 'Arduino Uno R3', 'arduino-uno-r3', 4.8, 1250, '2026-10-10 17:58:48.178447', '2026-10-10 17:58:48.178447');
INSERT INTO public.products VALUES (2, 'Arduino Nano V3', 'arduino-nano-v3', 4.7, 980, '2026-10-10 17:58:48.178447', '2026-10-10 17:58:48.178447');
INSERT INTO public.products VALUES (3, 'Arduino Mega 2560', 'arduino-mega-2560', 4.9, 450, '2026-10-10 17:58:48.178447', '2026-10-10 17:58:48.178447');
INSERT INTO public.products VALUES (4, 'ESP32 DevKit V1', 'esp32-devkit-v1', 4.9, 2100, '2026-10-10 17:58:48.178447', '2026-10-10 17:58:48.178447');
INSERT INTO public.products VALUES (5, 'ESP8266 NodeMCU', 'esp8266-nodemcu', 4.6, 1580, '2026-10-10 17:58:48.178447', '2026-10-10 17:58:48.178447');
INSERT INTO public.products VALUES (6, 'Raspberry Pi 4 Model B 4GB', 'raspberry-pi-4-4gb', 4.9, 850, '2026-10-10 17:58:48.180437', '2026-10-10 17:58:48.180437');
INSERT INTO public.products VALUES (7, 'Raspberry Pi 3 Model B+', 'raspberry-pi-3-b-plus', 4.8, 620, '2026-10-10 17:58:48.180437', '2026-10-10 17:58:48.180437');
INSERT INTO public.products VALUES (8, 'Raspberry Pi Zero W', 'raspberry-pi-zero-w', 4.7, 340, '2026-10-10 17:58:48.180437', '2026-10-10 17:58:48.180437');
INSERT INTO public.products VALUES (9, 'DHT22 - Cáº£m biáº¿n nhiá»‡t Ä‘á»™ vÃ  Ä‘á»™ áº©m', 'dht22-sensor', 4.7, 1850, '2026-10-10 17:58:48.18074', '2026-10-10 17:58:48.18074');
INSERT INTO public.products VALUES (10, 'HC-SR04 - Cáº£m biáº¿n siÃªu Ã¢m', 'hc-sr04-ultrasonic', 4.6, 2230, '2026-10-10 17:58:48.18074', '2026-10-10 17:58:48.18074');
INSERT INTO public.products VALUES (11, 'BMP280 - Cáº£m biáº¿n Ã¡p suáº¥t vÃ  nhiá»‡t Ä‘á»™', 'bmp280-pressure', 4.8, 670, '2026-10-10 17:58:48.18074', '2026-10-10 17:58:48.18074');
INSERT INTO public.products VALUES (12, 'MPU6050 - Cáº£m biáº¿n gia tá»‘c vÃ  con quay há»“i chuyá»ƒn', 'mpu6050-imu', 4.8, 1120, '2026-10-10 17:58:48.18074', '2026-10-10 17:58:48.18074');
INSERT INTO public.products VALUES (13, 'MQ-2 - Cáº£m biáº¿n khÃ­ gas', 'mq2-gas-sensor', 4.5, 890, '2026-10-10 17:58:48.18074', '2026-10-10 17:58:48.18074');
INSERT INTO public.products VALUES (14, 'Module Buck LM2596 DC-DC', 'lm2596-buck-converter', 4.7, 1560, '2026-10-10 17:58:48.181171', '2026-10-10 17:58:48.181171');
INSERT INTO public.products VALUES (15, 'Module Boost MT3608', 'mt3608-boost-converter', 4.6, 1230, '2026-10-10 17:58:48.181171', '2026-10-10 17:58:48.181171');
INSERT INTO public.products VALUES (16, 'TP4056 - Module sáº¡c pin Lithium', 'tp4056-charger', 4.8, 2450, '2026-10-10 17:58:48.181171', '2026-10-10 17:58:48.181171');
INSERT INTO public.products VALUES (17, 'AMS1117-3.3V - Module nguá»“n 3.3V', 'ams1117-3v3-regulator', 4.5, 890, '2026-10-10 17:58:48.181171', '2026-10-10 17:58:48.181171');
INSERT INTO public.products VALUES (18, 'NRF24L01+ - Module RF 2.4GHz', 'nrf24l01-rf-module', 4.6, 1340, '2026-10-10 17:58:48.181389', '2026-10-10 17:58:48.181389');
INSERT INTO public.products VALUES (19, 'HC-05 - Module Bluetooth', 'hc05-bluetooth', 4.7, 1780, '2026-10-10 17:58:48.181389', '2026-10-10 17:58:48.181389');
INSERT INTO public.products VALUES (20, 'SIM800L - Module GSM/GPRS', 'sim800l-gsm', 4.4, 420, '2026-10-10 17:58:48.181389', '2026-10-10 17:58:48.181389');
INSERT INTO public.products VALUES (21, 'LoRa SX1278 433MHz', 'lora-sx1278-433mhz', 4.8, 560, '2026-10-10 17:58:48.181389', '2026-10-10 17:58:48.181389');
INSERT INTO public.products VALUES (22, 'LCD 16x2 I2C xanh lÃ¡', 'lcd-16x2-i2c-green', 4.7, 1890, '2026-10-10 17:58:48.181662', '2026-10-10 17:58:48.181662');
INSERT INTO public.products VALUES (23, 'OLED 0.96" I2C 128x64', 'oled-096-i2c-128x64', 4.9, 2340, '2026-10-10 17:58:48.181662', '2026-10-10 17:58:48.181662');
INSERT INTO public.products VALUES (24, 'TFT LCD 2.4" SPI cáº£m á»©ng', 'tft-24-spi-touch', 4.6, 780, '2026-10-10 17:58:48.181662', '2026-10-10 17:58:48.181662');
INSERT INTO public.products VALUES (25, 'L298N - Driver Ä‘á»™ng cÆ¡ DC', 'l298n-motor-driver', 4.7, 1450, '2026-10-10 17:58:48.181914', '2026-10-10 17:58:48.181914');
INSERT INTO public.products VALUES (26, 'Äá»™ng cÆ¡ Servo SG90', 'servo-sg90', 4.6, 2120, '2026-10-10 17:58:48.181914', '2026-10-10 17:58:48.181914');
INSERT INTO public.products VALUES (27, 'Äá»™ng cÆ¡ giáº£m tá»‘c 12V', 'dc-geared-motor-12v', 4.8, 670, '2026-10-10 17:58:48.181914', '2026-10-10 17:58:48.181914');
INSERT INTO public.products VALUES (28, 'Äá»™ng cÆ¡ Stepper 28BYJ-48', 'stepper-28byj48', 4.5, 890, '2026-10-10 17:58:48.181914', '2026-10-10 17:58:48.181914');


--
-- Data for Name: reviews; Type: TABLE DATA; Schema: public; Owner: -
--

INSERT INTO public.reviews VALUES (1, 2, 1, 5, 'Arduino Uno cháº¥t lÆ°á»£ng tá»‘t, ship nhanh, Ä‘Ã³ng gÃ³i cáº©n tháº­n. Ráº¥t hÃ i lÃ²ng!', '2026-10-10 17:58:48.186986');
INSERT INTO public.reviews VALUES (2, 3, 1, 5, 'Sáº£n pháº©m chÃ­nh hÃ£ng, hoáº¡t Ä‘á»™ng á»•n Ä‘á»‹nh. Shop tÆ° váº¥n nhiá»‡t tÃ¬nh.', '2026-10-10 17:58:48.186986');
INSERT INTO public.reviews VALUES (3, 2, 4, 5, 'ESP32 ráº¥t máº¡nh, WiFi á»•n Ä‘á»‹nh, láº­p trÃ¬nh dá»… dÃ ng. GiÃ¡ cáº£ pháº£i chÄƒng.', '2026-10-10 17:58:48.186986');
INSERT INTO public.reviews VALUES (4, 3, 9, 4, 'DHT22 Ä‘o chÃ­nh xÃ¡c, giao tiáº¿p Ä‘Æ¡n giáº£n. ÄÃ³ng gÃ³i tá»‘t.', '2026-10-10 17:58:48.186986');
INSERT INTO public.reviews VALUES (5, 2, 10, 5, 'HC-SR04 hoáº¡t Ä‘á»™ng tá»‘t, Ä‘o khoáº£ng cÃ¡ch chÃ­nh xÃ¡c. GiÃ¡ ráº».', '2026-10-10 17:58:48.186986');
INSERT INTO public.reviews VALUES (6, 3, 23, 5, 'OLED sáº¯c nÃ©t, Ä‘á»™ tÆ°Æ¡ng pháº£n cao, ráº¥t Ä‘áº¹p. Recommend!', '2026-10-10 17:58:48.186986');
INSERT INTO public.reviews VALUES (7, 2, 19, 4, 'HC-05 káº¿t ná»‘i á»•n Ä‘á»‹nh, dá»… cáº¥u hÃ¬nh. Tá»‘t vá»›i giÃ¡ tiá»n nÃ y.', '2026-10-10 17:58:48.186986');


--
-- Data for Name: user_notifications; Type: TABLE DATA; Schema: public; Owner: -
--



--
-- Data for Name: users; Type: TABLE DATA; Schema: public; Owner: -
--

INSERT INTO public.users VALUES (1, 'he@gmail.com', '\\\/vBNA9ceMSWDEOlMRe7SU3iuPLhWsXwJmSfeLlber9zQ2', 'Admin', '0901234567', NULL, NULL, NULL, 'Male', NULL, 'ROLE_ADMIN', true, '2026-10-10 17:58:48.174639', '2026-10-10 17:58:48.174639', NULL);
INSERT INTO public.users VALUES (2, 'user@gmail.com', '\\\/vBNA9ceMSWDEOlMRe7SU3iuPLhWsXwJmSfeLlber9zQ2', 'Nguyá»…n VÄƒn A', '0987654321', NULL, NULL, NULL, 'Male', NULL, 'ROLE_USER', true, '2026-10-10 17:58:48.174639', '2026-10-10 17:58:48.174639', NULL);
INSERT INTO public.users VALUES (3, 'maria@gmail.com', '\\\/vBNA9ceMSWDEOlMRe7SU3iuPLhWsXwJmSfeLlber9zQ2', 'Tráº§n Thá»‹ B', '0912345678', NULL, NULL, NULL, 'Female', NULL, 'ROLE_USER', true, '2026-10-10 17:58:48.174639', '2026-10-10 17:58:48.174639', NULL);
INSERT INTO public.users VALUES (5, 'shopper_2621@daln.vn', '$2a$10$B4xrJtCvCN6KH06CKcM1bePxWr9zx9IVIv7iJmmWfvowND2/.gunG', 'Khach Hang Test', NULL, NULL, NULL, NULL, NULL, NULL, 'ROLE_USER', true, '2026-10-12 00:58:33.074044', '2026-10-12 00:58:33.074044', false);
INSERT INTO public.users VALUES (6, 'testadmin_1540@daln.vn', '$2a$10$3hjzADwV/vBNA9ceMSWDEOlMRe7SU3iuPLhWsXwJmSfeLlber9zQ2', 'Test Admin', NULL, NULL, NULL, NULL, NULL, NULL, 'ROLE_USER', true, '2026-10-12 02:19:18.827606', '2026-10-12 02:19:18.827606', false);
INSERT INTO public.users VALUES (7, 'phapdamanh@gmail.com', '$2a$10$jOJFtci0vtJJgsD/fFQHi.2CMkk9lUPVpErkOeXPzKAuatBw5n8la', 'Dam Anh Phap', NULL, NULL, NULL, NULL, NULL, NULL, 'ROLE_ADMIN', true, '2026-10-12 02:21:48.175201', '2026-10-12 02:21:48.175201', true);


--
-- Name: articles_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('public.articles_id_seq', 1, false);


--
-- Name: cart_items_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('public.cart_items_id_seq', 1, true);


--
-- Name: carts_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('public.carts_id_seq', 2, true);


--
-- Name: categories_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('public.categories_id_seq', 8, true);


--
-- Name: chat_messages_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('public.chat_messages_id_seq', 40, true);


--
-- Name: chat_tool_logs_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('public.chat_tool_logs_id_seq', 17, true);


--
-- Name: inventory_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('public.inventory_id_seq', 1, false);


--
-- Name: notifications_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('public.notifications_id_seq', 1, false);


--
-- Name: order_items_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('public.order_items_id_seq', 1, false);


--
-- Name: orders_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('public.orders_id_seq', 1, false);


--
-- Name: payments_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('public.payments_id_seq', 1, false);


--
-- Name: product_variants_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('public.product_variants_id_seq', 38, true);


--
-- Name: products_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('public.products_id_seq', 28, true);


--
-- Name: reviews_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('public.reviews_id_seq', 7, true);


--
-- Name: users_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('public.users_id_seq', 7, true);


--
-- Name: articles articles_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.articles
    ADD CONSTRAINT articles_pkey PRIMARY KEY (id);


--
-- Name: articles articles_slug_key; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.articles
    ADD CONSTRAINT articles_slug_key UNIQUE (slug);


--
-- Name: cart_items cart_items_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.cart_items
    ADD CONSTRAINT cart_items_pkey PRIMARY KEY (id);


--
-- Name: carts carts_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.carts
    ADD CONSTRAINT carts_pkey PRIMARY KEY (id);


--
-- Name: carts carts_user_id_key; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.carts
    ADD CONSTRAINT carts_user_id_key UNIQUE (user_id);


--
-- Name: categories categories_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.categories
    ADD CONSTRAINT categories_pkey PRIMARY KEY (id);


--
-- Name: categories categories_slug_key; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.categories
    ADD CONSTRAINT categories_slug_key UNIQUE (slug);


--
-- Name: chat_messages chat_messages_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.chat_messages
    ADD CONSTRAINT chat_messages_pkey PRIMARY KEY (id);


--
-- Name: chat_sessions chat_sessions_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.chat_sessions
    ADD CONSTRAINT chat_sessions_pkey PRIMARY KEY (id);


--
-- Name: chat_tool_logs chat_tool_logs_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.chat_tool_logs
    ADD CONSTRAINT chat_tool_logs_pkey PRIMARY KEY (id);


--
-- Name: inventory inventory_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.inventory
    ADD CONSTRAINT inventory_pkey PRIMARY KEY (id);


--
-- Name: notifications notifications_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.notifications
    ADD CONSTRAINT notifications_pkey PRIMARY KEY (id);


--
-- Name: order_items order_items_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.order_items
    ADD CONSTRAINT order_items_pkey PRIMARY KEY (id);


--
-- Name: orders orders_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.orders
    ADD CONSTRAINT orders_pkey PRIMARY KEY (id);


--
-- Name: payments payments_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.payments
    ADD CONSTRAINT payments_pkey PRIMARY KEY (id);


--
-- Name: product_categories product_categories_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.product_categories
    ADD CONSTRAINT product_categories_pkey PRIMARY KEY (product_id, category_id);


--
-- Name: product_variants product_variants_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.product_variants
    ADD CONSTRAINT product_variants_pkey PRIMARY KEY (id);


--
-- Name: products products_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.products
    ADD CONSTRAINT products_pkey PRIMARY KEY (id);


--
-- Name: products products_slug_key; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.products
    ADD CONSTRAINT products_slug_key UNIQUE (slug);


--
-- Name: reviews reviews_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.reviews
    ADD CONSTRAINT reviews_pkey PRIMARY KEY (id);


--
-- Name: reviews reviews_user_id_product_id_key; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.reviews
    ADD CONSTRAINT reviews_user_id_product_id_key UNIQUE (user_id, product_id);


--
-- Name: reviews uk1nv3auyahyyy79hvtrcqgtfo9; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.reviews
    ADD CONSTRAINT uk1nv3auyahyyy79hvtrcqgtfo9 UNIQUE (user_id, product_id);


--
-- Name: inventory ukce3rbi3bfstbvvyne34c1dvyv; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.inventory
    ADD CONSTRAINT ukce3rbi3bfstbvvyne34c1dvyv UNIQUE (product_id);


--
-- Name: orders uknthkiu7pgmnqnu86i2jyoe2v7; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.orders
    ADD CONSTRAINT uknthkiu7pgmnqnu86i2jyoe2v7 UNIQUE (order_number);


--
-- Name: user_notifications user_notifications_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.user_notifications
    ADD CONSTRAINT user_notifications_pkey PRIMARY KEY (user_id, notification_id);


--
-- Name: users users_email_key; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.users
    ADD CONSTRAINT users_email_key UNIQUE (email);


--
-- Name: users users_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.users
    ADD CONSTRAINT users_pkey PRIMARY KEY (id);


--
-- Name: idx_orders_user; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_orders_user ON public.orders USING btree (user_id);


--
-- Name: idx_products_slug; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_products_slug ON public.products USING btree (slug);


--
-- Name: idx_reviews_product; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_reviews_product ON public.reviews USING btree (product_id);


--
-- Name: idx_users_email; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_users_email ON public.users USING btree (email);


--
-- Name: articles articles_author_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.articles
    ADD CONSTRAINT articles_author_id_fkey FOREIGN KEY (author_id) REFERENCES public.users(id) ON DELETE SET NULL;


--
-- Name: cart_items cart_items_cart_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.cart_items
    ADD CONSTRAINT cart_items_cart_id_fkey FOREIGN KEY (cart_id) REFERENCES public.carts(id) ON DELETE CASCADE;


--
-- Name: cart_items cart_items_product_variant_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.cart_items
    ADD CONSTRAINT cart_items_product_variant_id_fkey FOREIGN KEY (product_variant_id) REFERENCES public.product_variants(id) ON DELETE CASCADE;


--
-- Name: carts carts_user_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.carts
    ADD CONSTRAINT carts_user_id_fkey FOREIGN KEY (user_id) REFERENCES public.users(id) ON DELETE CASCADE;


--
-- Name: inventory fkq2yge7ebtfuvwufr6lwfwqy9l; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.inventory
    ADD CONSTRAINT fkq2yge7ebtfuvwufr6lwfwqy9l FOREIGN KEY (product_id) REFERENCES public.products(id);


--
-- Name: order_items order_items_order_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.order_items
    ADD CONSTRAINT order_items_order_id_fkey FOREIGN KEY (order_id) REFERENCES public.orders(id) ON DELETE CASCADE;


--
-- Name: order_items order_items_product_variant_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.order_items
    ADD CONSTRAINT order_items_product_variant_id_fkey FOREIGN KEY (product_variant_id) REFERENCES public.product_variants(id) ON DELETE SET NULL;


--
-- Name: orders orders_user_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.orders
    ADD CONSTRAINT orders_user_id_fkey FOREIGN KEY (user_id) REFERENCES public.users(id) ON DELETE SET NULL;


--
-- Name: product_categories product_categories_category_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.product_categories
    ADD CONSTRAINT product_categories_category_id_fkey FOREIGN KEY (category_id) REFERENCES public.categories(id) ON DELETE CASCADE;


--
-- Name: product_categories product_categories_product_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.product_categories
    ADD CONSTRAINT product_categories_product_id_fkey FOREIGN KEY (product_id) REFERENCES public.products(id) ON DELETE CASCADE;


--
-- Name: product_variants product_variants_product_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.product_variants
    ADD CONSTRAINT product_variants_product_id_fkey FOREIGN KEY (product_id) REFERENCES public.products(id) ON DELETE CASCADE;


--
-- Name: reviews reviews_product_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.reviews
    ADD CONSTRAINT reviews_product_id_fkey FOREIGN KEY (product_id) REFERENCES public.products(id) ON DELETE CASCADE;


--
-- Name: reviews reviews_user_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.reviews
    ADD CONSTRAINT reviews_user_id_fkey FOREIGN KEY (user_id) REFERENCES public.users(id) ON DELETE CASCADE;


--
-- Name: user_notifications user_notifications_notification_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.user_notifications
    ADD CONSTRAINT user_notifications_notification_id_fkey FOREIGN KEY (notification_id) REFERENCES public.notifications(id) ON DELETE CASCADE;


--
-- Name: user_notifications user_notifications_user_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.user_notifications
    ADD CONSTRAINT user_notifications_user_id_fkey FOREIGN KEY (user_id) REFERENCES public.users(id) ON DELETE CASCADE;


--
-- PostgreSQL database dump complete
--


