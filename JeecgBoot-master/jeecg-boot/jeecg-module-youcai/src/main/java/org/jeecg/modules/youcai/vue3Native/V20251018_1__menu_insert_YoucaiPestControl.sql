-- 注意：该页面对应的前台目录为views/youcai文件夹下
-- 如果你想更改到其他目录，请修改sql中component字段对应的值


-- 主菜单
INSERT INTO sys_permission(id, parent_id, name, url, component, component_name, redirect, menu_type, perms, perms_type, sort_no, always_show, icon, is_route, is_leaf, keep_alive, hidden, hide_tab, description, status, del_flag, rule_flag, create_by, create_time, update_by, update_time, internal_or_external)
VALUES ('176076597037701', NULL, '虫害防控表', '/youcai/youcaiPestControlList', 'youcai/YoucaiPestControlList', NULL, NULL, 0, NULL, '1', 0.00, 0, NULL, 1, 0, 0, 0, 0, NULL, '1', 0, 0, 'admin', '2025-10-18 13:39:30', NULL, NULL, 0);

-- 新增
INSERT INTO sys_permission(id, parent_id, name, url, component, is_route, component_name, redirect, menu_type, perms, perms_type, sort_no, always_show, icon, is_leaf, keep_alive, hidden, hide_tab, description, create_by, create_time, update_by, update_time, del_flag, rule_flag, status, internal_or_external)
VALUES ('176076597037802', '176076597037701', '添加虫害防控表', NULL, NULL, 0, NULL, NULL, 2, 'youcai:youcai_pest_control:add', '1', NULL, 0, NULL, 1, 0, 0, 0, NULL, 'admin', '2025-10-18 13:39:30', NULL, NULL, 0, 0, '1', 0);

-- 编辑
INSERT INTO sys_permission(id, parent_id, name, url, component, is_route, component_name, redirect, menu_type, perms, perms_type, sort_no, always_show, icon, is_leaf, keep_alive, hidden, hide_tab, description, create_by, create_time, update_by, update_time, del_flag, rule_flag, status, internal_or_external)
VALUES ('176076597037803', '176076597037701', '编辑虫害防控表', NULL, NULL, 0, NULL, NULL, 2, 'youcai:youcai_pest_control:edit', '1', NULL, 0, NULL, 1, 0, 0, 0, NULL, 'admin', '2025-10-18 13:39:30', NULL, NULL, 0, 0, '1', 0);

-- 删除
INSERT INTO sys_permission(id, parent_id, name, url, component, is_route, component_name, redirect, menu_type, perms, perms_type, sort_no, always_show, icon, is_leaf, keep_alive, hidden, hide_tab, description, create_by, create_time, update_by, update_time, del_flag, rule_flag, status, internal_or_external)
VALUES ('176076597037804', '176076597037701', '删除虫害防控表', NULL, NULL, 0, NULL, NULL, 2, 'youcai:youcai_pest_control:delete', '1', NULL, 0, NULL, 1, 0, 0, 0, NULL, 'admin', '2025-10-18 13:39:30', NULL, NULL, 0, 0, '1', 0);

-- 批量删除
INSERT INTO sys_permission(id, parent_id, name, url, component, is_route, component_name, redirect, menu_type, perms, perms_type, sort_no, always_show, icon, is_leaf, keep_alive, hidden, hide_tab, description, create_by, create_time, update_by, update_time, del_flag, rule_flag, status, internal_or_external)
VALUES ('176076597037805', '176076597037701', '批量删除虫害防控表', NULL, NULL, 0, NULL, NULL, 2, 'youcai:youcai_pest_control:deleteBatch', '1', NULL, 0, NULL, 1, 0, 0, 0, NULL, 'admin', '2025-10-18 13:39:30', NULL, NULL, 0, 0, '1', 0);

-- 导出excel
INSERT INTO sys_permission(id, parent_id, name, url, component, is_route, component_name, redirect, menu_type, perms, perms_type, sort_no, always_show, icon, is_leaf, keep_alive, hidden, hide_tab, description, create_by, create_time, update_by, update_time, del_flag, rule_flag, status, internal_or_external)
VALUES ('176076597037806', '176076597037701', '导出excel_虫害防控表', NULL, NULL, 0, NULL, NULL, 2, 'youcai:youcai_pest_control:exportXls', '1', NULL, 0, NULL, 1, 0, 0, 0, NULL, 'admin', '2025-10-18 13:39:30', NULL, NULL, 0, 0, '1', 0);

-- 导入excel
INSERT INTO sys_permission(id, parent_id, name, url, component, is_route, component_name, redirect, menu_type, perms, perms_type, sort_no, always_show, icon, is_leaf, keep_alive, hidden, hide_tab, description, create_by, create_time, update_by, update_time, del_flag, rule_flag, status, internal_or_external)
VALUES ('176076597037807', '176076597037701', '导入excel_虫害防控表', NULL, NULL, 0, NULL, NULL, 2, 'youcai:youcai_pest_control:importExcel', '1', NULL, 0, NULL, 1, 0, 0, 0, NULL, 'admin', '2025-10-18 13:39:30', NULL, NULL, 0, 0, '1', 0);

-- 角色授权（以 admin 角色为例，role_id 可替换）
INSERT INTO sys_role_permission (id, role_id, permission_id, data_rule_ids, operate_date, operate_ip) VALUES ('176076597037808', 'f6817f48af4fb3af11b9e8bf182f618b', '176076597037701', NULL, '2025-10-18 13:39:30', '127.0.0.1');
INSERT INTO sys_role_permission (id, role_id, permission_id, data_rule_ids, operate_date, operate_ip) VALUES ('176076597037809', 'f6817f48af4fb3af11b9e8bf182f618b', '176076597037802', NULL, '2025-10-18 13:39:30', '127.0.0.1');
INSERT INTO sys_role_permission (id, role_id, permission_id, data_rule_ids, operate_date, operate_ip) VALUES ('176076597037810', 'f6817f48af4fb3af11b9e8bf182f618b', '176076597037803', NULL, '2025-10-18 13:39:30', '127.0.0.1');
INSERT INTO sys_role_permission (id, role_id, permission_id, data_rule_ids, operate_date, operate_ip) VALUES ('176076597037811', 'f6817f48af4fb3af11b9e8bf182f618b', '176076597037804', NULL, '2025-10-18 13:39:30', '127.0.0.1');
INSERT INTO sys_role_permission (id, role_id, permission_id, data_rule_ids, operate_date, operate_ip) VALUES ('176076597037812', 'f6817f48af4fb3af11b9e8bf182f618b', '176076597037805', NULL, '2025-10-18 13:39:30', '127.0.0.1');
INSERT INTO sys_role_permission (id, role_id, permission_id, data_rule_ids, operate_date, operate_ip) VALUES ('176076597037813', 'f6817f48af4fb3af11b9e8bf182f618b', '176076597037806', NULL, '2025-10-18 13:39:30', '127.0.0.1');
INSERT INTO sys_role_permission (id, role_id, permission_id, data_rule_ids, operate_date, operate_ip) VALUES ('176076597037814', 'f6817f48af4fb3af11b9e8bf182f618b', '176076597037807', NULL, '2025-10-18 13:39:30', '127.0.0.1');