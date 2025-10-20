-- 注意：该页面对应的前台目录为views/youcai文件夹下
-- 如果你想更改到其他目录，请修改sql中component字段对应的值


-- 主菜单
INSERT INTO sys_permission(id, parent_id, name, url, component, component_name, redirect, menu_type, perms, perms_type, sort_no, always_show, icon, is_route, is_leaf, keep_alive, hidden, hide_tab, description, status, del_flag, rule_flag, create_by, create_time, update_by, update_time, internal_or_external)
VALUES ('176076593804501', NULL, '肥料投入表', '/youcai/youcaiFertilizerInputsList', 'youcai/YoucaiFertilizerInputsList', NULL, NULL, 0, NULL, '1', 0.00, 0, NULL, 1, 0, 0, 0, 0, NULL, '1', 0, 0, 'admin', '2025-10-18 13:38:58', NULL, NULL, 0);

-- 新增
INSERT INTO sys_permission(id, parent_id, name, url, component, is_route, component_name, redirect, menu_type, perms, perms_type, sort_no, always_show, icon, is_leaf, keep_alive, hidden, hide_tab, description, create_by, create_time, update_by, update_time, del_flag, rule_flag, status, internal_or_external)
VALUES ('176076593804502', '176076593804501', '添加肥料投入表', NULL, NULL, 0, NULL, NULL, 2, 'youcai:youcai_fertilizer_inputs:add', '1', NULL, 0, NULL, 1, 0, 0, 0, NULL, 'admin', '2025-10-18 13:38:58', NULL, NULL, 0, 0, '1', 0);

-- 编辑
INSERT INTO sys_permission(id, parent_id, name, url, component, is_route, component_name, redirect, menu_type, perms, perms_type, sort_no, always_show, icon, is_leaf, keep_alive, hidden, hide_tab, description, create_by, create_time, update_by, update_time, del_flag, rule_flag, status, internal_or_external)
VALUES ('176076593804503', '176076593804501', '编辑肥料投入表', NULL, NULL, 0, NULL, NULL, 2, 'youcai:youcai_fertilizer_inputs:edit', '1', NULL, 0, NULL, 1, 0, 0, 0, NULL, 'admin', '2025-10-18 13:38:58', NULL, NULL, 0, 0, '1', 0);

-- 删除
INSERT INTO sys_permission(id, parent_id, name, url, component, is_route, component_name, redirect, menu_type, perms, perms_type, sort_no, always_show, icon, is_leaf, keep_alive, hidden, hide_tab, description, create_by, create_time, update_by, update_time, del_flag, rule_flag, status, internal_or_external)
VALUES ('176076593804504', '176076593804501', '删除肥料投入表', NULL, NULL, 0, NULL, NULL, 2, 'youcai:youcai_fertilizer_inputs:delete', '1', NULL, 0, NULL, 1, 0, 0, 0, NULL, 'admin', '2025-10-18 13:38:58', NULL, NULL, 0, 0, '1', 0);

-- 批量删除
INSERT INTO sys_permission(id, parent_id, name, url, component, is_route, component_name, redirect, menu_type, perms, perms_type, sort_no, always_show, icon, is_leaf, keep_alive, hidden, hide_tab, description, create_by, create_time, update_by, update_time, del_flag, rule_flag, status, internal_or_external)
VALUES ('176076593804505', '176076593804501', '批量删除肥料投入表', NULL, NULL, 0, NULL, NULL, 2, 'youcai:youcai_fertilizer_inputs:deleteBatch', '1', NULL, 0, NULL, 1, 0, 0, 0, NULL, 'admin', '2025-10-18 13:38:58', NULL, NULL, 0, 0, '1', 0);

-- 导出excel
INSERT INTO sys_permission(id, parent_id, name, url, component, is_route, component_name, redirect, menu_type, perms, perms_type, sort_no, always_show, icon, is_leaf, keep_alive, hidden, hide_tab, description, create_by, create_time, update_by, update_time, del_flag, rule_flag, status, internal_or_external)
VALUES ('176076593804506', '176076593804501', '导出excel_肥料投入表', NULL, NULL, 0, NULL, NULL, 2, 'youcai:youcai_fertilizer_inputs:exportXls', '1', NULL, 0, NULL, 1, 0, 0, 0, NULL, 'admin', '2025-10-18 13:38:58', NULL, NULL, 0, 0, '1', 0);

-- 导入excel
INSERT INTO sys_permission(id, parent_id, name, url, component, is_route, component_name, redirect, menu_type, perms, perms_type, sort_no, always_show, icon, is_leaf, keep_alive, hidden, hide_tab, description, create_by, create_time, update_by, update_time, del_flag, rule_flag, status, internal_or_external)
VALUES ('176076593804507', '176076593804501', '导入excel_肥料投入表', NULL, NULL, 0, NULL, NULL, 2, 'youcai:youcai_fertilizer_inputs:importExcel', '1', NULL, 0, NULL, 1, 0, 0, 0, NULL, 'admin', '2025-10-18 13:38:58', NULL, NULL, 0, 0, '1', 0);

-- 角色授权（以 admin 角色为例，role_id 可替换）
INSERT INTO sys_role_permission (id, role_id, permission_id, data_rule_ids, operate_date, operate_ip) VALUES ('176076593804508', 'f6817f48af4fb3af11b9e8bf182f618b', '176076593804501', NULL, '2025-10-18 13:38:58', '127.0.0.1');
INSERT INTO sys_role_permission (id, role_id, permission_id, data_rule_ids, operate_date, operate_ip) VALUES ('176076593804509', 'f6817f48af4fb3af11b9e8bf182f618b', '176076593804502', NULL, '2025-10-18 13:38:58', '127.0.0.1');
INSERT INTO sys_role_permission (id, role_id, permission_id, data_rule_ids, operate_date, operate_ip) VALUES ('176076593804510', 'f6817f48af4fb3af11b9e8bf182f618b', '176076593804503', NULL, '2025-10-18 13:38:58', '127.0.0.1');
INSERT INTO sys_role_permission (id, role_id, permission_id, data_rule_ids, operate_date, operate_ip) VALUES ('176076593804511', 'f6817f48af4fb3af11b9e8bf182f618b', '176076593804504', NULL, '2025-10-18 13:38:58', '127.0.0.1');
INSERT INTO sys_role_permission (id, role_id, permission_id, data_rule_ids, operate_date, operate_ip) VALUES ('176076593804512', 'f6817f48af4fb3af11b9e8bf182f618b', '176076593804505', NULL, '2025-10-18 13:38:58', '127.0.0.1');
INSERT INTO sys_role_permission (id, role_id, permission_id, data_rule_ids, operate_date, operate_ip) VALUES ('176076593804613', 'f6817f48af4fb3af11b9e8bf182f618b', '176076593804506', NULL, '2025-10-18 13:38:58', '127.0.0.1');
INSERT INTO sys_role_permission (id, role_id, permission_id, data_rule_ids, operate_date, operate_ip) VALUES ('176076593804614', 'f6817f48af4fb3af11b9e8bf182f618b', '176076593804507', NULL, '2025-10-18 13:38:58', '127.0.0.1');