SET NAMES utf8mb4;
USE elm2;

DROP TEMPORARY TABLE IF EXISTS tmp_reset_business_ids;
CREATE TEMPORARY TABLE tmp_reset_business_ids AS
SELECT businessId
FROM business
WHERE businessName LIKE 'Codex%'
   OR businessName LIKE 'CartShop%'
   OR businessName IN (
        '食满堂·牛肉饭',
        '晨光早餐铺',
        '城市跑腿站',
        '火山汉堡',
        '半糖甜品屋',
        '快味简餐',
        '津味小吃馆',
        '一碗热汤面',
        '暖心粥铺',
        '香酥炸鸡站'
   );

DELETE FROM orderDetailet
WHERE orderId IN (
    SELECT orderId
    FROM orders
    WHERE businessId IN (SELECT businessId FROM tmp_reset_business_ids)
);

DELETE FROM orders
WHERE businessId IN (SELECT businessId FROM tmp_reset_business_ids);

DELETE FROM cart
WHERE businessId IN (SELECT businessId FROM tmp_reset_business_ids);

DELETE FROM food
WHERE businessId IN (SELECT businessId FROM tmp_reset_business_ids);

DELETE FROM business
WHERE businessId IN (SELECT businessId FROM tmp_reset_business_ids);

DELETE FROM VirtualWalletDetails
WHERE fromWalletId IN (
        SELECT walletId FROM VirtualWallet WHERE userId LIKE 'merchant_codex_%' OR userId LIKE 'demo_biz_%'
    )
   OR toWalletId IN (
        SELECT walletId FROM VirtualWallet WHERE userId LIKE 'merchant_codex_%' OR userId LIKE 'demo_biz_%'
    );

DELETE FROM overdraft_records
WHERE walletId IN (
    SELECT walletId FROM VirtualWallet WHERE userId LIKE 'merchant_codex_%' OR userId LIKE 'demo_biz_%'
);

DELETE FROM vip_cards
WHERE userId LIKE 'merchant_codex_%' OR userId LIKE 'demo_biz_%';

DELETE FROM VirtualWallet
WHERE userId LIKE 'merchant_codex_%' OR userId LIKE 'demo_biz_%';

DELETE FROM user
WHERE userId LIKE 'merchant_codex_%' OR userId LIKE 'demo_biz_%';

INSERT INTO user (userId, password, userName, userSex, userImg, delTag) VALUES
('demo_biz_meishi', 'password', '食满堂商家', 1, NULL, 1),
('demo_biz_zaocan', 'password', '晨光早餐商家', 1, NULL, 1),
('demo_biz_paotui', 'password', '城市跑腿商家', 1, NULL, 1),
('demo_biz_hanbao', 'password', '火山汉堡商家', 1, NULL, 1),
('demo_biz_tianpin', 'password', '半糖甜品商家', 1, NULL, 1),
('demo_biz_jiancan', 'password', '快味简餐商家', 1, NULL, 1),
('demo_biz_xiaochi', 'password', '津味小吃商家', 1, NULL, 1),
('demo_biz_mian', 'password', '热汤面馆商家', 1, NULL, 1),
('demo_biz_zhou', 'password', '暖心粥铺商家', 1, NULL, 1),
('demo_biz_zhaji', 'password', '香酥炸鸡商家', 1, NULL, 1);

INSERT INTO business (
    businessName,
    businessAddress,
    businessExplain,
    businessImg,
    orderTypeId,
    starPrice,
    deliveryPrice,
    remarks,
    userId
) VALUES
('食满堂·牛肉饭', '大学城一食堂南侧', '现炒现做的家常盖饭，主打牛肉饭和下饭小炒。', NULL, 1, 20.00, 4.00, '推荐午餐', 'demo_biz_meishi'),
('晨光早餐铺', '大学城生活区东门', '豆浆、包子和热粥现做现卖，适合早八前快速下单。', NULL, 2, 8.00, 2.00, '早餐推荐', 'demo_biz_zaocan'),
('城市跑腿站', '大学城综合服务中心', '证件代取、超市代买和药品急送，30分钟内响应。', NULL, 3, 12.00, 3.00, '同城帮买帮送', 'demo_biz_paotui'),
('火山汉堡', '大学城商业街B区', '汉堡炸鸡和小食拼盘，适合多人分享。', NULL, 4, 18.00, 5.00, '热门快餐', 'demo_biz_hanbao'),
('半糖甜品屋', '大学城商业街甜品区', '奶茶、双皮奶和水果甜品，主打低糖清爽口味。', NULL, 5, 15.00, 3.00, '下午茶优选', 'demo_biz_tianpin'),
('快味简餐', '大学城实验楼后街', '便当和简餐出餐快，适合赶课前下单。', NULL, 6, 16.00, 4.00, '工作日刚需', 'demo_biz_jiancan'),
('津味小吃馆', '大学城北门小吃街', '煎饼果子、锅巴菜和豆腐脑，天津风味集中上桌。', NULL, 7, 10.00, 3.00, '地方特色', 'demo_biz_xiaochi'),
('一碗热汤面', '大学城图书馆西侧', '汤面、拌面和馄饨热气腾腾，夜宵也适合。', NULL, 8, 14.00, 3.00, '热汤现煮', 'demo_biz_mian'),
('暖心粥铺', '大学城宿舍区南门', '粥、蒸点和清淡小菜，适合早餐与夜宵。', NULL, 9, 9.00, 2.00, '清淡养胃', 'demo_biz_zhou'),
('香酥炸鸡站', '大学城商业街夜市口', '炸鸡、鸡排和小串现炸出炉，适合追剧配餐。', NULL, 10, 19.00, 5.00, '夜宵热门', 'demo_biz_zhaji');

INSERT INTO food (foodName, foodExplain, foodPrice, businessId, remarks, valid)
SELECT '红烧牛肉饭', '慢炖牛腩搭配时蔬和溏心蛋', 22.00, businessId, '招牌推荐', 1
FROM business WHERE businessName = '食满堂·牛肉饭';
INSERT INTO food (foodName, foodExplain, foodPrice, businessId, remarks, valid)
SELECT '黑椒鸡腿饭', '黑椒酱汁配整只去骨鸡腿', 19.00, businessId, '热销', 1
FROM business WHERE businessName = '食满堂·牛肉饭';
INSERT INTO food (foodName, foodExplain, foodPrice, businessId, remarks, valid)
SELECT '番茄肥牛饭', '酸甜番茄汁配肥牛片', 21.00, businessId, '下饭', 1
FROM business WHERE businessName = '食满堂·牛肉饭';

INSERT INTO food (foodName, foodExplain, foodPrice, businessId, remarks, valid)
SELECT '鲜肉包+豆浆', '两只鲜肉包搭配现磨豆浆', 8.00, businessId, '早餐套餐', 1
FROM business WHERE businessName = '晨光早餐铺';
INSERT INTO food (foodName, foodExplain, foodPrice, businessId, remarks, valid)
SELECT '鸡蛋灌饼', '现烙薄饼夹生菜和火腿', 7.50, businessId, '现做', 1
FROM business WHERE businessName = '晨光早餐铺';
INSERT INTO food (foodName, foodExplain, foodPrice, businessId, remarks, valid)
SELECT '小米南瓜粥', '软糯香甜，适合清晨暖胃', 4.50, businessId, '清淡', 1
FROM business WHERE businessName = '晨光早餐铺';

INSERT INTO food (foodName, foodExplain, foodPrice, businessId, remarks, valid)
SELECT '文件代取', '代取快递、资料和证件，限校内', 6.00, businessId, '即时服务', 1
FROM business WHERE businessName = '城市跑腿站';
INSERT INTO food (foodName, foodExplain, foodPrice, businessId, remarks, valid)
SELECT '超市代买', '零食饮料和日用品代买服务费', 8.00, businessId, '即时服务', 1
FROM business WHERE businessName = '城市跑腿站';
INSERT INTO food (foodName, foodExplain, foodPrice, businessId, remarks, valid)
SELECT '药品急送', '感冒药、创可贴等应急代送', 10.00, businessId, '夜间可下单', 1
FROM business WHERE businessName = '城市跑腿站';

INSERT INTO food (foodName, foodExplain, foodPrice, businessId, remarks, valid)
SELECT '双层牛肉堡', '双层牛肉饼配芝士和酸黄瓜', 24.00, businessId, '招牌推荐', 1
FROM business WHERE businessName = '火山汉堡';
INSERT INTO food (foodName, foodExplain, foodPrice, businessId, remarks, valid)
SELECT '脆鸡腿堡', '香脆鸡腿排搭配生菜蛋黄酱', 19.00, businessId, '热销', 1
FROM business WHERE businessName = '火山汉堡';
INSERT INTO food (foodName, foodExplain, foodPrice, businessId, remarks, valid)
SELECT '薯条拼盘', '粗薯配番茄酱和甜辣酱', 12.00, businessId, '加购优选', 1
FROM business WHERE businessName = '火山汉堡';

INSERT INTO food (foodName, foodExplain, foodPrice, businessId, remarks, valid)
SELECT '杨枝甘露', '芒果西柚配西米，微冰更好喝', 18.00, businessId, '人气甜品', 1
FROM business WHERE businessName = '半糖甜品屋';
INSERT INTO food (foodName, foodExplain, foodPrice, businessId, remarks, valid)
SELECT '芒果双皮奶', '奶香绵密，顶部铺满鲜切芒果', 16.00, businessId, '低糖推荐', 1
FROM business WHERE businessName = '半糖甜品屋';
INSERT INTO food (foodName, foodExplain, foodPrice, businessId, remarks, valid)
SELECT '黑糖珍珠奶茶', '现煮珍珠搭配鲜牛乳和黑糖', 14.00, businessId, '经典款', 1
FROM business WHERE businessName = '半糖甜品屋';

INSERT INTO food (foodName, foodExplain, foodPrice, businessId, remarks, valid)
SELECT '黄焖鸡米饭', '香菇土豆配鸡腿肉，酱香浓郁', 18.00, businessId, '销量第一', 1
FROM business WHERE businessName = '快味简餐';
INSERT INTO food (foodName, foodExplain, foodPrice, businessId, remarks, valid)
SELECT '台式卤肉饭', '肥瘦相间卤肉搭配酸菜卤蛋', 17.00, businessId, '经典简餐', 1
FROM business WHERE businessName = '快味简餐';
INSERT INTO food (foodName, foodExplain, foodPrice, businessId, remarks, valid)
SELECT '香菇滑鸡便当', '滑鸡配时蔬，分量扎实', 19.00, businessId, '便当推荐', 1
FROM business WHERE businessName = '快味简餐';

INSERT INTO food (foodName, foodExplain, foodPrice, businessId, remarks, valid)
SELECT '天津煎饼果子', '现摊绿豆面饼配薄脆鸡蛋', 9.00, businessId, '天津味', 1
FROM business WHERE businessName = '津味小吃馆';
INSERT INTO food (foodName, foodExplain, foodPrice, businessId, remarks, valid)
SELECT '老味锅巴菜', '卤汁入味，搭配麻酱更香', 8.50, businessId, '地方特色', 1
FROM business WHERE businessName = '津味小吃馆';
INSERT INTO food (foodName, foodExplain, foodPrice, businessId, remarks, valid)
SELECT '炸糕豆腐脑', '甜咸可选，早餐夜宵都适合', 10.00, businessId, '人气组合', 1
FROM business WHERE businessName = '津味小吃馆';

INSERT INTO food (foodName, foodExplain, foodPrice, businessId, remarks, valid)
SELECT '老坛酸菜牛肉面', '酸辣开胃，汤底浓郁', 16.00, businessId, '招牌面', 1
FROM business WHERE businessName = '一碗热汤面';
INSERT INTO food (foodName, foodExplain, foodPrice, businessId, remarks, valid)
SELECT '番茄鸡蛋面', '家常番茄汤底，口味温和', 14.00, businessId, '清爽', 1
FROM business WHERE businessName = '一碗热汤面';
INSERT INTO food (foodName, foodExplain, foodPrice, businessId, remarks, valid)
SELECT '骨汤馄饨面', '馄饨和细面双拼更满足', 18.00, businessId, '夜宵推荐', 1
FROM business WHERE businessName = '一碗热汤面';

INSERT INTO food (foodName, foodExplain, foodPrice, businessId, remarks, valid)
SELECT '南瓜小米粥', '软糯香甜，配小菜更下饭', 6.00, businessId, '暖胃', 1
FROM business WHERE businessName = '暖心粥铺';
INSERT INTO food (foodName, foodExplain, foodPrice, businessId, remarks, valid)
SELECT '皮蛋瘦肉粥', '咸香顺滑，早餐夜宵都合适', 9.00, businessId, '热销', 1
FROM business WHERE businessName = '暖心粥铺';
INSERT INTO food (foodName, foodExplain, foodPrice, businessId, remarks, valid)
SELECT '鲜虾蔬菜粥', '口感清爽，配料丰富', 12.00, businessId, '轻食', 1
FROM business WHERE businessName = '暖心粥铺';

INSERT INTO food (foodName, foodExplain, foodPrice, businessId, remarks, valid)
SELECT '吮指原味鸡', '现炸外酥里嫩，适合多人分享', 18.00, businessId, '热门单品', 1
FROM business WHERE businessName = '香酥炸鸡站';
INSERT INTO food (foodName, foodExplain, foodPrice, businessId, remarks, valid)
SELECT '秘制鸡排', '大块鸡排现炸出锅，香辣可选', 16.00, businessId, '解馋', 1
FROM business WHERE businessName = '香酥炸鸡站';
INSERT INTO food (foodName, foodExplain, foodPrice, businessId, remarks, valid)
SELECT '黄金鸡米花', '酥脆小食，适合加购', 12.00, businessId, '小食', 1
FROM business WHERE businessName = '香酥炸鸡站';

DROP TEMPORARY TABLE IF EXISTS tmp_reset_business_ids;
