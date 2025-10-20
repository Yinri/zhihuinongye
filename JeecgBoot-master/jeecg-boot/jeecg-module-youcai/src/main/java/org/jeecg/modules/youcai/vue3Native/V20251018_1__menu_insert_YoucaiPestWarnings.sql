-- 注意：该页面对应的前台目录为views/youcai文件夹下
-- 如果你想更改到其他目录，请修改sql中component字段对应的值


-- 主菜单
INSERT INTO sys_permission(id, parent_id, name, url, component, component_name, redirect, menu_type, perms, perms_type, sort_no, always_show, icon, is_route, is_leaf, keep_alive, hidden, hide_tab, description, status, del_flag, rule_flag, create_by, create_time, update_by, update_time, internal_or_external)
VALUES ('176076599877901', NULL, '虫害预警表', '/youcai/youcaiPestWarningsList', 'youcai/YoucaiPestWarningsList', NULL, NULL, 0, NULL, '1', 0.00, 0, NULL, 1, 0, 0, 0, 0, NULL, '1', 0, 0, 'admin', '2025-10-18 13:39:58', NULL, NULL, 0);

-- 新增
INSERT INTO sys_permission(id, parent_id, name, url, component, is_route, component_name, redirect, menu_type, perms, perms_type, sort_no, always_show, icon, is_leaf, keep_alive, hidden, hide_tab, description, create_by, create_time, update_by, update_time, del_flag, rule_flag, status, internal_or_external)
VALUES ('176076599878002', '176076599877901', '添加虫害预警表', NULL, NULL, 0, NULL, NULL, 2, 'youcai:youcai_pest_warnings:add', '1', NULL, 0, NULL, 1, 0, 0, 0, NULL, 'admin', '2025-10-18 13:39:58', NULL, NULL, 0, 0, '1', 0);

-- 编辑
INSERT INTO sys_permission(id, parent_id, name, url, component, is_route, component_name, redirect, menu_type, perms, perms_type, sort_no, always_show, icon, is_leaf, keep_alive, hidden, hide_tab, description, create_by, create_time, update_by, update_time, del_flag, rule_flag, status, internal_or_external)
VALUES ('176076599878003', '176076599877901', '编辑虫害预警表', NULL, NULL, 0, NULL, NULL, 2, 'youcai:youcai_pest_warnings:edit', '1', NULL, 0, NULL, 1, 0, 0, 0, NULL, 'admin', '2025-10-18 13:39:58', NULL, NULL, 0, 0, '1', 0);

-- 删除
INSERT INTO sys_permission(id, parent_id, name, url, component, is_route, component_name, redirect, menu_type, perms, perms_type, sort_no, always_show, icon, is_leaf, keep_alive, hidden, hide_tab, description, create_by, create_time, update_by, update_time, del_flag, rule_flag, status, internal_or_external)
VALUES ('176076599878004', '176076599877901', '删除虫害预警表', NULL, NULL, 0, NULL, NULL, 2, 'youcai:youcai_pest_warnings:delete', '1', NULL, 0, NULL, 1, 0, 0, 0, NULL, 'admin', '2025-10-18 13:39:58', NULL, NULL, 0, 0, '1', 0);

-- 批量删除
INSERT INTO sys_permission(id, parent_id, name, url, component, is_route, component_name, redirect, menu_type, perms, perms_type, sort_no, always_show, icon, is_leaf, keep_alive, hidden, hide_tab, description, create_by, create_time, update_by, update_time, del_flag, rule_flag, status, internal_or_external)
VALUES ('176076599878005', '176076599877901', '批量删除虫害预警表', NULL, NULL, 0, NULL, NULL, 2, 'youcai:youcai_pest_warnings:deleteBatch', '1', NULL, 0, NULL, 1, 0, 0, 0, NULL, 'admin', '2025-10-18 13:39:58', NULL, NULL, 0, 0, '1', 0);

-- 导出excel
INSERT INTO sys_permission(id, parent_id, name, url, component, is_route, component_name, redirect, menu_type, perms, perms_type, sort_no, always_show, icon, is_leaf, keep_alive, hidden, hide_tab, description, create_by, create_time, update_by, update_time, del_flag, rule_flag, status, internal_or_external)
VALUES ('176076599878006', '176076599877901', '导出excel_虫害预警表', NULL, NULL, 0, NULL, NULL, 2, 'youcai:youcai_pest_warnings:exportXls', '1', NULL, 0, NULL, 1, 0, 0, 0, NULL, 'admin', '2025-10-18 13:39:58', NULL, NULL, 0, 0, '1', 0);

-- 导入excel
INSERT INTO sys_permission(id, parent_id, name, url, component, is_route, component_name, redirect, menu_type, perms, perms_type, sort_no, always_show, icon, is_leaf, keep_alive, hidden, hide_tab, description, create_by, create_time, update_by, update_time, del_flag, rule_flag, status, internal_or_external)
VALUES ('176076599878007', '176076599877901', '导入excel_虫害预警表', NULL, NULL, 0, NULL, NULL, 2, 'youcai:youcai_pest_warnings:importExcel', '1', NULL, 0, NULL, 1, 0, 0, 0, NULL, 'admin', '2025-10-18 13:39:58', NULL, NULL, 0, 0, '1', 0);

-- 角色授权（以 admin 角色为例，role_id 可替换）
INSERT INTO sys_role_permission (id, role_id, permission_id, data_rule_ids, operate_date, operate_ip) VALUES ('176076599878008', 'f6817f48af4fb3af11b9e8bf182f618b', '176076599877901', NULL, '2025-10-18 13:39:58', '127.0.0.1');
INSERT INTO sys_role_permission (id, role_id, permission_id, data_rule_ids, operate_date, operate_ip) VALUES ('176076599878009', 'f6817f48af4fb3af11b9e8bf182f618b', '176076599878002', NULL, '2025-10-18 13:39:58', '127.0.0.1');
INSERT INTO sys_role_permission (id, role_id, permission_id, data_rule_ids, operate_date, operate_ip) VALUES ('176076599878010', 'f6817f48af4fb3af11b9e8bf182f618b', '176076599878003', NULL, '2025-10-18 13:39:58', '127.0.0.1');
INSERT INTO sys_role_permission (id, role_id, permission_id, data_rule_ids, operate_date, operate_ip) VALUES ('176076599878011', 'f6817f48af4fb3af11b9e8bf182f618b', '176076599878004', NULL, '2025-10-18 13:39:58', '127.0.0.1');
INSERT INTO sys_role_permission (id, role_id, permission_id, data_rule_ids, operate_date, operate_ip) VALUES ('176076599878012', 'f6817f48af4fb3af11b9e8bf182f618b', '176076599878005', NULL, '2025-10-18 13:39:58', '127.0.0.1');
INSERT INTO sys_role_permission (id, role_id, permission_id, data_rule_ids, operate_date, operate_ip) VALUES ('176076599878013', 'f6817f48af4fb3af11b9e8bf182f618b', '176076599878006', NULL, '2025-10-18 13:39:58', '127.0.0.1');
INSERT INTO sys_role_permission (id, role_id, permission_id, data_rule_ids, operate_date, operate_ip) VALUES ('176076599878014', 'f6817f48af4fb3af11b9e8bf182f618b', '176076599878007', NULL, '2025-10-18 13:39:58', '127.0.0.1');