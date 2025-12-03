-- 注意：该页面对应的前台目录为views/youcai文件夹下
-- 如果你想更改到其他目录，请修改sql中component字段对应的值


-- 主菜单
INSERT INTO sys_permission(id, parent_id, name, url, component, component_name, redirect, menu_type, perms, perms_type, sort_no, always_show, icon, is_route, is_leaf, keep_alive, hidden, hide_tab, description, status, del_flag, rule_flag, create_by, create_time, update_by, update_time, internal_or_external)
VALUES ('176466808617901', NULL, 'crop_nutrient_demand', '/youcai/cropNutrientDemandList', 'youcai/CropNutrientDemandList', NULL, NULL, 0, NULL, '1', 0.00, 0, NULL, 1, 0, 0, 0, 0, NULL, '1', 0, 0, 'admin', '2025-12-02 17:34:46', NULL, NULL, 0);

-- 新增
INSERT INTO sys_permission(id, parent_id, name, url, component, is_route, component_name, redirect, menu_type, perms, perms_type, sort_no, always_show, icon, is_leaf, keep_alive, hidden, hide_tab, description, create_by, create_time, update_by, update_time, del_flag, rule_flag, status, internal_or_external)
VALUES ('176466808617902', '176466808617901', '添加crop_nutrient_demand', NULL, NULL, 0, NULL, NULL, 2, 'youcai:crop_nutrient_demand:add', '1', NULL, 0, NULL, 1, 0, 0, 0, NULL, 'admin', '2025-12-02 17:34:46', NULL, NULL, 0, 0, '1', 0);

-- 编辑
INSERT INTO sys_permission(id, parent_id, name, url, component, is_route, component_name, redirect, menu_type, perms, perms_type, sort_no, always_show, icon, is_leaf, keep_alive, hidden, hide_tab, description, create_by, create_time, update_by, update_time, del_flag, rule_flag, status, internal_or_external)
VALUES ('176466808617903', '176466808617901', '编辑crop_nutrient_demand', NULL, NULL, 0, NULL, NULL, 2, 'youcai:crop_nutrient_demand:edit', '1', NULL, 0, NULL, 1, 0, 0, 0, NULL, 'admin', '2025-12-02 17:34:46', NULL, NULL, 0, 0, '1', 0);

-- 删除
INSERT INTO sys_permission(id, parent_id, name, url, component, is_route, component_name, redirect, menu_type, perms, perms_type, sort_no, always_show, icon, is_leaf, keep_alive, hidden, hide_tab, description, create_by, create_time, update_by, update_time, del_flag, rule_flag, status, internal_or_external)
VALUES ('176466808617904', '176466808617901', '删除crop_nutrient_demand', NULL, NULL, 0, NULL, NULL, 2, 'youcai:crop_nutrient_demand:delete', '1', NULL, 0, NULL, 1, 0, 0, 0, NULL, 'admin', '2025-12-02 17:34:46', NULL, NULL, 0, 0, '1', 0);

-- 批量删除
INSERT INTO sys_permission(id, parent_id, name, url, component, is_route, component_name, redirect, menu_type, perms, perms_type, sort_no, always_show, icon, is_leaf, keep_alive, hidden, hide_tab, description, create_by, create_time, update_by, update_time, del_flag, rule_flag, status, internal_or_external)
VALUES ('176466808617905', '176466808617901', '批量删除crop_nutrient_demand', NULL, NULL, 0, NULL, NULL, 2, 'youcai:crop_nutrient_demand:deleteBatch', '1', NULL, 0, NULL, 1, 0, 0, 0, NULL, 'admin', '2025-12-02 17:34:46', NULL, NULL, 0, 0, '1', 0);

-- 导出excel
INSERT INTO sys_permission(id, parent_id, name, url, component, is_route, component_name, redirect, menu_type, perms, perms_type, sort_no, always_show, icon, is_leaf, keep_alive, hidden, hide_tab, description, create_by, create_time, update_by, update_time, del_flag, rule_flag, status, internal_or_external)
VALUES ('176466808617906', '176466808617901', '导出excel_crop_nutrient_demand', NULL, NULL, 0, NULL, NULL, 2, 'youcai:crop_nutrient_demand:exportXls', '1', NULL, 0, NULL, 1, 0, 0, 0, NULL, 'admin', '2025-12-02 17:34:46', NULL, NULL, 0, 0, '1', 0);

-- 导入excel
INSERT INTO sys_permission(id, parent_id, name, url, component, is_route, component_name, redirect, menu_type, perms, perms_type, sort_no, always_show, icon, is_leaf, keep_alive, hidden, hide_tab, description, create_by, create_time, update_by, update_time, del_flag, rule_flag, status, internal_or_external)
VALUES ('176466808617907', '176466808617901', '导入excel_crop_nutrient_demand', NULL, NULL, 0, NULL, NULL, 2, 'youcai:crop_nutrient_demand:importExcel', '1', NULL, 0, NULL, 1, 0, 0, 0, NULL, 'admin', '2025-12-02 17:34:46', NULL, NULL, 0, 0, '1', 0);

-- 角色授权（以 admin 角色为例，role_id 可替换）
INSERT INTO sys_role_permission (id, role_id, permission_id, data_rule_ids, operate_date, operate_ip) VALUES ('176466808617908', 'f6817f48af4fb3af11b9e8bf182f618b', '176466808617901', NULL, '2025-12-02 17:34:46', '127.0.0.1');
INSERT INTO sys_role_permission (id, role_id, permission_id, data_rule_ids, operate_date, operate_ip) VALUES ('176466808617909', 'f6817f48af4fb3af11b9e8bf182f618b', '176466808617902', NULL, '2025-12-02 17:34:46', '127.0.0.1');
INSERT INTO sys_role_permission (id, role_id, permission_id, data_rule_ids, operate_date, operate_ip) VALUES ('176466808617910', 'f6817f48af4fb3af11b9e8bf182f618b', '176466808617903', NULL, '2025-12-02 17:34:46', '127.0.0.1');
INSERT INTO sys_role_permission (id, role_id, permission_id, data_rule_ids, operate_date, operate_ip) VALUES ('176466808617911', 'f6817f48af4fb3af11b9e8bf182f618b', '176466808617904', NULL, '2025-12-02 17:34:46', '127.0.0.1');
INSERT INTO sys_role_permission (id, role_id, permission_id, data_rule_ids, operate_date, operate_ip) VALUES ('176466808617912', 'f6817f48af4fb3af11b9e8bf182f618b', '176466808617905', NULL, '2025-12-02 17:34:46', '127.0.0.1');
INSERT INTO sys_role_permission (id, role_id, permission_id, data_rule_ids, operate_date, operate_ip) VALUES ('176466808618013', 'f6817f48af4fb3af11b9e8bf182f618b', '176466808617906', NULL, '2025-12-02 17:34:46', '127.0.0.1');
INSERT INTO sys_role_permission (id, role_id, permission_id, data_rule_ids, operate_date, operate_ip) VALUES ('176466808618014', 'f6817f48af4fb3af11b9e8bf182f618b', '176466808617907', NULL, '2025-12-02 17:34:46', '127.0.0.1');