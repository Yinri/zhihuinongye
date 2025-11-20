-- 注意：该页面对应的前台目录为views/youcai文件夹下
-- 如果你想更改到其他目录，请修改sql中component字段对应的值


-- 主菜单
INSERT INTO sys_permission(id, parent_id, name, url, component, component_name, redirect, menu_type, perms, perms_type, sort_no, always_show, icon, is_route, is_leaf, keep_alive, hidden, hide_tab, description, status, del_flag, rule_flag, create_by, create_time, update_by, update_time, internal_or_external)
VALUES ('176233486575901', NULL, 'youcai_historical_yield', '/youcai/youcaiHistoricalYieldList', 'youcai/YoucaiHistoricalYieldList', NULL, NULL, 0, NULL, '1', 0.00, 0, NULL, 1, 0, 0, 0, 0, NULL, '1', 0, 0, 'admin', '2025-11-05 17:27:45', NULL, NULL, 0);

-- 新增
INSERT INTO sys_permission(id, parent_id, name, url, component, is_route, component_name, redirect, menu_type, perms, perms_type, sort_no, always_show, icon, is_leaf, keep_alive, hidden, hide_tab, description, create_by, create_time, update_by, update_time, del_flag, rule_flag, status, internal_or_external)
VALUES ('176233486575902', '176233486575901', '添加youcai_historical_yield', NULL, NULL, 0, NULL, NULL, 2, 'youcai:youcai_historical_yield:add', '1', NULL, 0, NULL, 1, 0, 0, 0, NULL, 'admin', '2025-11-05 17:27:45', NULL, NULL, 0, 0, '1', 0);

-- 编辑
INSERT INTO sys_permission(id, parent_id, name, url, component, is_route, component_name, redirect, menu_type, perms, perms_type, sort_no, always_show, icon, is_leaf, keep_alive, hidden, hide_tab, description, create_by, create_time, update_by, update_time, del_flag, rule_flag, status, internal_or_external)
VALUES ('176233486575903', '176233486575901', '编辑youcai_historical_yield', NULL, NULL, 0, NULL, NULL, 2, 'youcai:youcai_historical_yield:edit', '1', NULL, 0, NULL, 1, 0, 0, 0, NULL, 'admin', '2025-11-05 17:27:45', NULL, NULL, 0, 0, '1', 0);

-- 删除
INSERT INTO sys_permission(id, parent_id, name, url, component, is_route, component_name, redirect, menu_type, perms, perms_type, sort_no, always_show, icon, is_leaf, keep_alive, hidden, hide_tab, description, create_by, create_time, update_by, update_time, del_flag, rule_flag, status, internal_or_external)
VALUES ('176233486575904', '176233486575901', '删除youcai_historical_yield', NULL, NULL, 0, NULL, NULL, 2, 'youcai:youcai_historical_yield:delete', '1', NULL, 0, NULL, 1, 0, 0, 0, NULL, 'admin', '2025-11-05 17:27:45', NULL, NULL, 0, 0, '1', 0);

-- 批量删除
INSERT INTO sys_permission(id, parent_id, name, url, component, is_route, component_name, redirect, menu_type, perms, perms_type, sort_no, always_show, icon, is_leaf, keep_alive, hidden, hide_tab, description, create_by, create_time, update_by, update_time, del_flag, rule_flag, status, internal_or_external)
VALUES ('176233486575905', '176233486575901', '批量删除youcai_historical_yield', NULL, NULL, 0, NULL, NULL, 2, 'youcai:youcai_historical_yield:deleteBatch', '1', NULL, 0, NULL, 1, 0, 0, 0, NULL, 'admin', '2025-11-05 17:27:45', NULL, NULL, 0, 0, '1', 0);

-- 导出excel
INSERT INTO sys_permission(id, parent_id, name, url, component, is_route, component_name, redirect, menu_type, perms, perms_type, sort_no, always_show, icon, is_leaf, keep_alive, hidden, hide_tab, description, create_by, create_time, update_by, update_time, del_flag, rule_flag, status, internal_or_external)
VALUES ('176233486575906', '176233486575901', '导出excel_youcai_historical_yield', NULL, NULL, 0, NULL, NULL, 2, 'youcai:youcai_historical_yield:exportXls', '1', NULL, 0, NULL, 1, 0, 0, 0, NULL, 'admin', '2025-11-05 17:27:45', NULL, NULL, 0, 0, '1', 0);

-- 导入excel
INSERT INTO sys_permission(id, parent_id, name, url, component, is_route, component_name, redirect, menu_type, perms, perms_type, sort_no, always_show, icon, is_leaf, keep_alive, hidden, hide_tab, description, create_by, create_time, update_by, update_time, del_flag, rule_flag, status, internal_or_external)
VALUES ('176233486575907', '176233486575901', '导入excel_youcai_historical_yield', NULL, NULL, 0, NULL, NULL, 2, 'youcai:youcai_historical_yield:importExcel', '1', NULL, 0, NULL, 1, 0, 0, 0, NULL, 'admin', '2025-11-05 17:27:45', NULL, NULL, 0, 0, '1', 0);

-- 角色授权（以 admin 角色为例，role_id 可替换）
INSERT INTO sys_role_permission (id, role_id, permission_id, data_rule_ids, operate_date, operate_ip) VALUES ('176233486575908', 'f6817f48af4fb3af11b9e8bf182f618b', '176233486575901', NULL, '2025-11-05 17:27:45', '127.0.0.1');
INSERT INTO sys_role_permission (id, role_id, permission_id, data_rule_ids, operate_date, operate_ip) VALUES ('176233486575909', 'f6817f48af4fb3af11b9e8bf182f618b', '176233486575902', NULL, '2025-11-05 17:27:45', '127.0.0.1');
INSERT INTO sys_role_permission (id, role_id, permission_id, data_rule_ids, operate_date, operate_ip) VALUES ('176233486576010', 'f6817f48af4fb3af11b9e8bf182f618b', '176233486575903', NULL, '2025-11-05 17:27:45', '127.0.0.1');
INSERT INTO sys_role_permission (id, role_id, permission_id, data_rule_ids, operate_date, operate_ip) VALUES ('176233486576011', 'f6817f48af4fb3af11b9e8bf182f618b', '176233486575904', NULL, '2025-11-05 17:27:45', '127.0.0.1');
INSERT INTO sys_role_permission (id, role_id, permission_id, data_rule_ids, operate_date, operate_ip) VALUES ('176233486576012', 'f6817f48af4fb3af11b9e8bf182f618b', '176233486575905', NULL, '2025-11-05 17:27:45', '127.0.0.1');
INSERT INTO sys_role_permission (id, role_id, permission_id, data_rule_ids, operate_date, operate_ip) VALUES ('176233486576013', 'f6817f48af4fb3af11b9e8bf182f618b', '176233486575906', NULL, '2025-11-05 17:27:45', '127.0.0.1');
INSERT INTO sys_role_permission (id, role_id, permission_id, data_rule_ids, operate_date, operate_ip) VALUES ('176233486576014', 'f6817f48af4fb3af11b9e8bf182f618b', '176233486575907', NULL, '2025-11-05 17:27:45', '127.0.0.1');