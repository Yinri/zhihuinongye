-- 注意：该页面对应的前台目录为views/youcai文件夹下
-- 如果你想更改到其他目录，请修改sql中component字段对应的值


-- 主菜单
INSERT INTO sys_permission(id, parent_id, name, url, component, component_name, redirect, menu_type, perms, perms_type, sort_no, always_show, icon, is_route, is_leaf, keep_alive, hidden, hide_tab, description, status, del_flag, rule_flag, create_by, create_time, update_by, update_time, internal_or_external)
VALUES ('176076597987401', NULL, '倒伏风险预警表', '/youcai/youcaiLodgingRiskWarningList', 'youcai/YoucaiLodgingRiskWarningList', NULL, NULL, 0, NULL, '1', 0.00, 0, NULL, 1, 0, 0, 0, 0, NULL, '1', 0, 0, 'admin', '2025-10-18 13:39:39', NULL, NULL, 0);

-- 新增
INSERT INTO sys_permission(id, parent_id, name, url, component, is_route, component_name, redirect, menu_type, perms, perms_type, sort_no, always_show, icon, is_leaf, keep_alive, hidden, hide_tab, description, create_by, create_time, update_by, update_time, del_flag, rule_flag, status, internal_or_external)
VALUES ('176076597987402', '176076597987401', '添加倒伏风险预警表', NULL, NULL, 0, NULL, NULL, 2, 'youcai:youcai_lodging_risk_warning:add', '1', NULL, 0, NULL, 1, 0, 0, 0, NULL, 'admin', '2025-10-18 13:39:39', NULL, NULL, 0, 0, '1', 0);

-- 编辑
INSERT INTO sys_permission(id, parent_id, name, url, component, is_route, component_name, redirect, menu_type, perms, perms_type, sort_no, always_show, icon, is_leaf, keep_alive, hidden, hide_tab, description, create_by, create_time, update_by, update_time, del_flag, rule_flag, status, internal_or_external)
VALUES ('176076597987403', '176076597987401', '编辑倒伏风险预警表', NULL, NULL, 0, NULL, NULL, 2, 'youcai:youcai_lodging_risk_warning:edit', '1', NULL, 0, NULL, 1, 0, 0, 0, NULL, 'admin', '2025-10-18 13:39:39', NULL, NULL, 0, 0, '1', 0);

-- 删除
INSERT INTO sys_permission(id, parent_id, name, url, component, is_route, component_name, redirect, menu_type, perms, perms_type, sort_no, always_show, icon, is_leaf, keep_alive, hidden, hide_tab, description, create_by, create_time, update_by, update_time, del_flag, rule_flag, status, internal_or_external)
VALUES ('176076597987404', '176076597987401', '删除倒伏风险预警表', NULL, NULL, 0, NULL, NULL, 2, 'youcai:youcai_lodging_risk_warning:delete', '1', NULL, 0, NULL, 1, 0, 0, 0, NULL, 'admin', '2025-10-18 13:39:39', NULL, NULL, 0, 0, '1', 0);

-- 批量删除
INSERT INTO sys_permission(id, parent_id, name, url, component, is_route, component_name, redirect, menu_type, perms, perms_type, sort_no, always_show, icon, is_leaf, keep_alive, hidden, hide_tab, description, create_by, create_time, update_by, update_time, del_flag, rule_flag, status, internal_or_external)
VALUES ('176076597987405', '176076597987401', '批量删除倒伏风险预警表', NULL, NULL, 0, NULL, NULL, 2, 'youcai:youcai_lodging_risk_warning:deleteBatch', '1', NULL, 0, NULL, 1, 0, 0, 0, NULL, 'admin', '2025-10-18 13:39:39', NULL, NULL, 0, 0, '1', 0);

-- 导出excel
INSERT INTO sys_permission(id, parent_id, name, url, component, is_route, component_name, redirect, menu_type, perms, perms_type, sort_no, always_show, icon, is_leaf, keep_alive, hidden, hide_tab, description, create_by, create_time, update_by, update_time, del_flag, rule_flag, status, internal_or_external)
VALUES ('176076597987406', '176076597987401', '导出excel_倒伏风险预警表', NULL, NULL, 0, NULL, NULL, 2, 'youcai:youcai_lodging_risk_warning:exportXls', '1', NULL, 0, NULL, 1, 0, 0, 0, NULL, 'admin', '2025-10-18 13:39:39', NULL, NULL, 0, 0, '1', 0);

-- 导入excel
INSERT INTO sys_permission(id, parent_id, name, url, component, is_route, component_name, redirect, menu_type, perms, perms_type, sort_no, always_show, icon, is_leaf, keep_alive, hidden, hide_tab, description, create_by, create_time, update_by, update_time, del_flag, rule_flag, status, internal_or_external)
VALUES ('176076597987407', '176076597987401', '导入excel_倒伏风险预警表', NULL, NULL, 0, NULL, NULL, 2, 'youcai:youcai_lodging_risk_warning:importExcel', '1', NULL, 0, NULL, 1, 0, 0, 0, NULL, 'admin', '2025-10-18 13:39:39', NULL, NULL, 0, 0, '1', 0);

-- 角色授权（以 admin 角色为例，role_id 可替换）
INSERT INTO sys_role_permission (id, role_id, permission_id, data_rule_ids, operate_date, operate_ip) VALUES ('176076597987408', 'f6817f48af4fb3af11b9e8bf182f618b', '176076597987401', NULL, '2025-10-18 13:39:39', '127.0.0.1');
INSERT INTO sys_role_permission (id, role_id, permission_id, data_rule_ids, operate_date, operate_ip) VALUES ('176076597987409', 'f6817f48af4fb3af11b9e8bf182f618b', '176076597987402', NULL, '2025-10-18 13:39:39', '127.0.0.1');
INSERT INTO sys_role_permission (id, role_id, permission_id, data_rule_ids, operate_date, operate_ip) VALUES ('176076597987410', 'f6817f48af4fb3af11b9e8bf182f618b', '176076597987403', NULL, '2025-10-18 13:39:39', '127.0.0.1');
INSERT INTO sys_role_permission (id, role_id, permission_id, data_rule_ids, operate_date, operate_ip) VALUES ('176076597987411', 'f6817f48af4fb3af11b9e8bf182f618b', '176076597987404', NULL, '2025-10-18 13:39:39', '127.0.0.1');
INSERT INTO sys_role_permission (id, role_id, permission_id, data_rule_ids, operate_date, operate_ip) VALUES ('176076597987412', 'f6817f48af4fb3af11b9e8bf182f618b', '176076597987405', NULL, '2025-10-18 13:39:39', '127.0.0.1');
INSERT INTO sys_role_permission (id, role_id, permission_id, data_rule_ids, operate_date, operate_ip) VALUES ('176076597987413', 'f6817f48af4fb3af11b9e8bf182f618b', '176076597987406', NULL, '2025-10-18 13:39:39', '127.0.0.1');
INSERT INTO sys_role_permission (id, role_id, permission_id, data_rule_ids, operate_date, operate_ip) VALUES ('176076597987414', 'f6817f48af4fb3af11b9e8bf182f618b', '176076597987407', NULL, '2025-10-18 13:39:39', '127.0.0.1');