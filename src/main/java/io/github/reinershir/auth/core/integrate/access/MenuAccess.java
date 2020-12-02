package io.github.reinershir.auth.core.integrate.access;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.springframework.beans.BeanUtils;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.core.PreparedStatementSetter;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import io.github.reinershir.auth.core.integrate.vo.MenuVO;
import io.github.reinershir.auth.core.model.Menu;

public class MenuAccess extends AbstractAccess<Menu>{

	JdbcTemplate jdbcTemplate;
	String tableName;
	String roleTableName;
	MenuRowMapper mapper = new MenuRowMapper();
	
	//TODO SELECT * FROM TABLE、UPDATE DELETE FROM等封装成方法
	public MenuAccess(JdbcTemplate jdbcTemplate,String tableName,String roleTableName) {
		super(jdbcTemplate,tableName);
		this.jdbcTemplate=jdbcTemplate;
		this.tableName=tableName;
		this.roleTableName=roleTableName;
	}
	
	public List<Menu> qureyList(@Nullable Long parentId){
		List<Menu> list = new LinkedList<>();
		
		if(parentId==null) {
			list =  jdbcTemplate.query("SELECT * FROM "+tableName+" ORDER BY LEFT_VALUE ASC", mapper);
		}else {
			Menu parent = selectById(parentId);
			list = jdbcTemplate.query("SELECT * FROM "+tableName+" WHERE LEFT_VALUE>? AND RIGHT_VALUE > ? ORDER BY LEFT_VALUE", mapper,parent.getLeftValue(),parent.getRightValue());
		}
		return convertToTree(list);
	}
	
	/**
	 * @Title: convertToTree
	 * @Description:   将左右值列表转换为树形结构,(由于左右值结构数据查出来的是有顺序的，所以可以按left和right值判断层级关系)
	 * @author xh
	 * @date 2020年12月2日
	 * @param list
	 * @return
	 */
	private List<Menu> convertToTree(List<Menu> list ){
		List<Menu> resultList = new LinkedList<>();
		//记录上一个元素的菜单层级
		Integer beforeLevel = 1;
		for (Menu menu : list) {
			if(beforeLevel==menu.getLevel()-1) {
				//根据层级关系装配菜单数据
				assemblingChildMenu(resultList,menu);
			}else {
				//如果是父菜单直接加进来
				resultList.add(menu);
			}
			beforeLevel = menu.getLevel();
		}
		return resultList;
	}
	
	/**
	 * @Title: assemblingChildMenu
	 * @Description: 根据菜单层级装配为数形结构数据  
	 * @author xh
	 * @date 2020年12月2日
	 * @param resultList 已装配好的数据
	 * @param menu 要查找的数据
	 */
	private void assemblingChildMenu(List<Menu> resultList,Menu menu) {
		if(!CollectionUtils.isEmpty(resultList)&&menu!=null) {
			for (Menu parent : resultList) {
				//判断是否是该菜单的子节点
				if(menu.getLeftValue()>parent.getLeftValue()&&menu.getRightValue()<parent.getRightValue()) {
					//双重验证，再通过层级判断下
					if(menu.getLevel()-1==parent.getLevel()) {
						parent.getChildren().add(menu);
						break;
					}else {
						//如果层级不对，说明该节点在它子节点下面
						assemblingChildMenu(parent.getChildren(),menu);
					}
				}
			}
		}
	}
	
	public Long selectCount(@Nullable String menuName) {
		return super.selectCount(menuName, "NAME");
	}
	
	/**
	 * @Title: insertMenu
	 * @Description: 添加菜单数据
	 * @author xh
	 * @date 2020年12月1日
	 * @param menu 要添加的菜单数据对象
	 * @param parentId 指定要添加的父节点ID，即菜单会添加为该菜单ID的子菜单
	 * @return 1 = true
	 */
	@Transactional
	public int insertMenu(@Nonnull MenuVO menuVO,@Nullable Long parentId) {
		int result =-1;
		if(menuVO!=null) {
			Menu menu = new Menu();
			BeanUtils.copyProperties(menuVO,menu);
			KeyHolder holder = new GeneratedKeyHolder();
			//如果是添加初始节点
			if(parentId==null) {
				Menu first = getFirstNode();
				if(first==null) {
					//如果没有一级菜单，使用默认值
					menu.setLeftValue(1);
					menu.setRightValue(2);
					menu.setLevel(1);
				}else {
					//先更新左右节点值
					jdbcTemplate.update("UPDATE "+tableName+" SET LEFT_VALUE=LEFT_VALUE+2 WHERE LEFT_VALUE > ?",first.getRightValue());
					jdbcTemplate.update(" UPDATE "+tableName+" SET RIGHT_VALUE=RIGHT_VALUE+2 WHERE RIGHT_VALUE > ?",first.getRightValue());
					menu.setLeftValue(first.getRightValue()+1);
					menu.setRightValue(first.getRightValue()+2);
					menu.setLevel(first.getLevel());
				}
			}else {
				//如果是添加到指定节点下面
				Menu parent = selectById(parentId);
				//计算受影响节点
				jdbcTemplate.update("UPDATE "+tableName+" SET LEFT_VALUE=LEFT_VALUE+2 WHERE LEFT_VALUE >= ?",parent.getRightValue());
				jdbcTemplate.update("UPDATE "+tableName+" SET RIGHT_VALUE=RIGHT_VALUE+2 WHERE RIGHT_VALUE >= ?",parent.getRightValue());
				menu.setLeftValue(parent.getRightValue());
				menu.setRightValue(parent.getRightValue()+1);
				menu.setLevel(parent.getLevel()+1);
			}
			String sql = "INSERT INTO "+tableName+"(NAME,URL,ICON,PERMISSION_CODES,DESCRIPTION,PROPERTY,CREATE_DATE,LEFT_VALUE,RIGHT_VALUE,LEVEL) VALUES(?,?,?,?,?,?,?,?,?,?)";
			result = jdbcTemplate.update(new PreparedStatementCreator() {
				@Override
				public PreparedStatement createPreparedStatement(Connection connection) throws SQLException {
					PreparedStatement ps = connection.prepareStatement(sql,Statement.RETURN_GENERATED_KEYS);
					ps.setString(1, menu.getName());
					ps.setString(2, menu.getUrl());
					ps.setString(3, menu.getIcon());
					ps.setString(4, menu.getPermissionCodes());
					ps.setString(5, menu.getDescription());
					ps.setString(6, menu.getProperty());
					ps.setDate(7, new java.sql.Date(new Date().getTime()));
					ps.setInt(8, menu.getLeftValue());
					ps.setInt(9, menu.getRightValue());
					ps.setInt(10, menu.getLevel());
					return ps;
				}
			},holder);
			menu.setId(holder.getKey().longValue());
		}
		return result;
	}
	
	private Menu getFirstNode() {
		StringBuilder sql = new StringBuilder("SELECT * FROM ");
		sql.append(tableName);
		sql.append(" WHERE LEFT_VALUE = (SELECT MAX(M.LEFT_VALUE) FROM ");
		sql.append(tableName);
		sql.append(" M WHERE M.LEVEL = 1)");
		try {
			return jdbcTemplate.queryForObject(sql.toString(), mapper);
		} catch (DataAccessException e) {
			return null;
		}
	}
	
	public Menu selectById(Long id) {
		try {
			return jdbcTemplate.queryForObject("SELECT * FROM "+tableName+" WHERE ID = ?", mapper,id);
		} catch (DataAccessException e) {
			return null;
		}
	}
	
	@Transactional
	public int updateById(@Nonnull MenuVO menuVO) {
		if(menuVO!=null&&menuVO.getId()!=null) {
			Menu menu = new Menu();
			BeanUtils.copyProperties(menuVO,menu);
			int result =-1;
			result = jdbcTemplate.update("UPDATE "+tableName+" SET NAME=?,URL=?,ICON=?,DESCRIPTION=?,PROPERTY=?,UPDATE_DATE=? WHERE ID = ?", new PreparedStatementSetter() {
				
				@Override
				public void setValues(PreparedStatement ps) throws SQLException {
					ps.setString(1, menu.getName());
					ps.setString(2,menu.getUrl());
					ps.setString(3, menu.getIcon());
					ps.setString(4, menu.getDescription());
					ps.setString(5, menu.getProperty());
					ps.setDate(6, new java.sql.Date(new Date().getTime()));
					ps.setLong(7, menu.getId());
				}
			});
			StringBuilder sql = new StringBuilder("UPDATE ");
			sql.append(roleTableName);
			sql.append("_PERMISSION SET PERMISSION_CODES = (SELECT PERMISSION_CODES FROM ");
			sql.append(tableName);
			sql.append(" WHERE ID = ?) WHERE MENU_ID = ?");
			jdbcTemplate.update(sql.toString(),menu.getId(),menu.getId());
			return result;
		}
		return -1;
	}
	
	@Transactional
	public int deleteById(Long id) {
		int result =-1;
		Menu menu = selectById(id);
		if(menu!=null) {
			//删除节点
			result = jdbcTemplate.update("DELETE FROM "+tableName+" WHERE LEFT_VALUE >=? and RIGHT_VALUE <=?",menu.getLeftValue(),menu.getRightValue());
			//修改受影响的节点
			jdbcTemplate.update("update MENU set LEFT_VALUE=LEFT_VALUE-(? - ? + 1) where LEFT_VALUE > ?",menu.getRightValue(),menu.getLeftValue(),menu.getLeftValue());
			jdbcTemplate.update("update MENU set RIGHT_VALUE=RIGHT_VALUE-(? - ? + 1) where RIGHT_VALUE > ?",menu.getRightValue(),menu.getLeftValue(),menu.getRightValue());
		}
		return result;
	}
	
	
	public class MenuRowMapper implements  RowMapper<Menu>{

		@Override
		public Menu mapRow(ResultSet rs, int rowNum) throws SQLException {
			Menu menu = new Menu();
			menu.setId(rs.getLong("ID"));
			menu.setCreateDate(rs.getDate("CREATE_DATE"));
			menu.setDescription(rs.getString("DESCRIPTION"));
			menu.setName(rs.getString("NAME"));
			menu.setIcon(rs.getString("ICON"));
			menu.setLeftValue(rs.getInt("LEFT_VALUE"));
			menu.setRightValue(rs.getInt("RIGHT_VALUE"));
			menu.setPermissionCodes(rs.getString("PERMISSION_CODES"));
			menu.setProperty(rs.getString("PROPERTY"));
			menu.setLevel(rs.getInt("LEVEL"));
			menu.setUpdateDate(rs.getDate("UPDATE_DATE"));
			return menu;
		}
		
	}
}
