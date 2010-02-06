/**
 * Copyright (c) 2000-2010 Liferay, Inc. All rights reserved.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.liferay.portal.upgrade.util;

import com.liferay.portal.kernel.upgrade.StagnantRowException;
import com.liferay.portal.kernel.upgrade.util.UpgradeColumn;
import com.liferay.portal.kernel.upgrade.util.UpgradeTable;
import com.liferay.portal.kernel.util.Validator;
import com.liferay.portal.tools.comparator.ColumnsComparator;

import java.sql.PreparedStatement;
import java.sql.ResultSet;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * <a href="DefaultUpgradeTableImpl.java.html"><b><i>View Source</i></b></a>
 *
 * @author Alexander Chow
 * @author Bruno Farache
 */
public class DefaultUpgradeTableImpl
	extends BaseUpgradeTableImpl implements UpgradeTable {

	public String getExportedData(ResultSet rs) throws Exception {
		StringBuilder sb = new StringBuilder();

		Object[][] columns = getColumns();

		for (int i = 0; i < columns.length; i++) {
			boolean last = false;

			if ((i + 1) == columns.length) {
				last = true;
			}

			if (_upgradeColumns[i] == null) {
				appendColumn(
					sb, rs, (String)columns[i][0], (Integer)columns[i][1],
					last);
			}
			else {
				try {
					Integer columnType = _upgradeColumns[i].getOldColumnType(
						(Integer)columns[i][1]);

					Object oldValue = getValue(
						rs, (String)columns[i][0], columnType);

					_upgradeColumns[i].setOldValue(oldValue);

					Object newValue = _upgradeColumns[i].getNewValue(oldValue);

					_upgradeColumns[i].setNewValue(newValue);

					appendColumn(sb, newValue, last);
				}
				catch (StagnantRowException sre) {
					_upgradeColumns[i].setNewValue(null);

					throw new StagnantRowException(
						"Column " + columns[i][0] + " with value " +
							sre.getMessage(),
						sre);
				}
			}
		}

		return sb.toString();
	}

	public void setColumn(
			PreparedStatement ps, int index, Integer type, String value)
		throws Exception {

		if (_upgradeColumns[index] != null) {
			if (getCreateSQL() == null) {
				type = _upgradeColumns[index].getOldColumnType(type);
			}
			else {
				type = _upgradeColumns[index].getNewColumnType(type);
			}
		}

		super.setColumn(ps, index, type, value);
	}

	protected DefaultUpgradeTableImpl(
		String tableName, Object[][] columns, UpgradeColumn... upgradeColumns) {

		super(tableName);

		// Sort the column names to ensure they're sorted based on the
		// constructor's list of columns to upgrade. This is needed if you
		// use TempUpgradeColumnImpl and need to ensure a column's temporary
		// value is populated in the correct order.

		columns = columns.clone();

		List<String> sortedColumnNames = new ArrayList<String>();

		for (UpgradeColumn upgradeColumn : upgradeColumns) {
			getSortedColumnName(sortedColumnNames, upgradeColumn);
		}

		if (sortedColumnNames.size() > 0) {
			Arrays.sort(columns, new ColumnsComparator(sortedColumnNames));
		}

		setColumns(columns);

		_upgradeColumns = new UpgradeColumn[columns.length];

		for (UpgradeColumn upgradeColumn : upgradeColumns) {
			prepareUpgradeColumns(upgradeColumn);
		}
	}

	protected void getSortedColumnName(
		List<String> sortedColumnNames, UpgradeColumn upgradeColumn) {

		if (upgradeColumn == null) {
			return;
		}

		String name = upgradeColumn.getName();

		if (Validator.isNotNull(name)) {
			sortedColumnNames.add(name);
		}
	}

	protected void prepareUpgradeColumns(UpgradeColumn upgradeColumn) {
		if (upgradeColumn == null) {
			return;
		}

		Object[][] columns = getColumns();

		for (int i = 0; i < columns.length; i++) {
			String name = (String)columns[i][0];

			if (upgradeColumn.isApplicable(name)) {
				_upgradeColumns[i] = upgradeColumn;
			}
		}
	}

	private UpgradeColumn[] _upgradeColumns;

}