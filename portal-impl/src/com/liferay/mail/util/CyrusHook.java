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

package com.liferay.mail.util;

import com.liferay.mail.NoSuchCyrusUserException;
import com.liferay.mail.model.CyrusUser;
import com.liferay.mail.model.CyrusVirtual;
import com.liferay.mail.model.Filter;
import com.liferay.mail.service.persistence.CyrusUserUtil;
import com.liferay.mail.service.persistence.CyrusVirtualUtil;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.util.FileUtil;
import com.liferay.portal.kernel.util.ProcessUtil;
import com.liferay.portal.kernel.util.PropsKeys;
import com.liferay.portal.kernel.util.StringBundler;
import com.liferay.portal.kernel.util.StringUtil;
import com.liferay.portal.kernel.util.Validator;
import com.liferay.portal.util.PropsUtil;

import java.io.File;

import java.util.List;

/**
 * <a href="CyrusHook.java.html"><b><i>View Source</i></b></a>
 *
 * @author Brian Wing Shun Chan
 */
public class CyrusHook implements Hook {

	public void addForward(
		long companyId, long userId, List<Filter> filters,
		List<String> emailAddresses, boolean leaveCopy) {

		try {
			if (emailAddresses != null) {
				String home = PropsUtil.get(PropsKeys.MAIL_HOOK_CYRUS_HOME);

				File file = new File(home + "/" + userId + ".procmail.forward");

				if ((filters.size() > 0) || (emailAddresses.size() > 0) ||
					(leaveCopy)) {

					StringBundler sb = new StringBundler();

					for (int i = 0; i < filters.size(); i++) {
						Filter filter = filters.get(i);

						sb.append(":0\n");
						sb.append("* ^(From|Cc|To).*");
						sb.append(filter.getEmailAddress());
						sb.append("\n");
						sb.append("| $DELIVER -e -a $USER -m user.$USER.");
						sb.append(filter.getFolder());
						sb.append("\n\n");
					}

					if (leaveCopy) {
						sb.append(":0 c\n");
						sb.append("| $DELIVER -e -a $USER -m user.$USER\n\n");
					}

					if (emailAddresses.size() > 0) {
						sb.append(":0\n");
						sb.append("!");

						for (String emailAddress : emailAddresses) {
							sb.append(" ");
							sb.append(emailAddress);
						}
					}

					String content = sb.toString();

					while (content.endsWith("\n")) {
						content = content.substring(0, content.length() - 1);
					}

					FileUtil.write(file, content);
				}
				else {
					file.delete();
				}
			}
		}
		catch (Exception e) {
			_log.error(e, e);
		}
	}

	public void addUser(
		long companyId, long userId, String password, String firstName,
		String middleName, String lastName, String emailAddress) {

		try {

			// User

			CyrusUser user = new CyrusUser(userId, password);

			CyrusUserUtil.update(user);

			// Virtual

			CyrusVirtual virtual = new CyrusVirtual(emailAddress, userId);

			CyrusVirtualUtil.update(virtual);

			// Expect

			String addUserCmd =
				PropsUtil.get(PropsKeys.MAIL_HOOK_CYRUS_ADD_USER);

			addUserCmd = StringUtil.replace(
				addUserCmd, "%1%", String.valueOf(userId));

			Runtime rt = Runtime.getRuntime();

			Process p = rt.exec(addUserCmd);

			ProcessUtil.close(p);
		}
		catch (Exception e) {
			_log.error(e, e);
		}
	}

	public void addVacationMessage(
		long companyId, long userId, String emailAddress,
		String vacationMessage) {

		try {
			String home = PropsUtil.get(PropsKeys.MAIL_HOOK_CYRUS_HOME);

			// Remove vacation cache

			new File(home + "/" + userId + ".vacation.cache").delete();

			// Update vacation message

			File vacation = new File(home + "/" + userId + ".vacation");

			if (Validator.isNull(vacationMessage)) {
				vacation.delete();
			}
			else {
				FileUtil.write(vacation, emailAddress + "\n" + vacationMessage);
			}
		}
		catch (Exception e) {
			_log.error(e, e);
		}
	}

	public void deleteEmailAddress(long companyId, long userId) {
		try {
			CyrusVirtualUtil.removeByUserId(userId);
		}
		catch (Exception e) {
			_log.error(e, e);
		}
	}

	public void deleteUser(long companyId, long userId) {
		try {

			// User

			try {
				CyrusUserUtil.remove(userId);
			}
			catch (NoSuchCyrusUserException nscue) {
			}

			// Virtual

			CyrusVirtualUtil.removeByUserId(userId);

			// Expect

			String deleteUserCmd =
				PropsUtil.get(PropsKeys.MAIL_HOOK_CYRUS_DELETE_USER);

			deleteUserCmd = StringUtil.replace(
				deleteUserCmd, "%1%", String.valueOf(userId));

			Runtime rt = Runtime.getRuntime();

			Process p = rt.exec(deleteUserCmd);

			ProcessUtil.close(p);

			// Procmail

			String home = PropsUtil.get(PropsKeys.MAIL_HOOK_CYRUS_HOME);

			File file = new File(home + "/" + userId + ".procmail.blocked");

			if (file.exists()) {
				file.delete();
			}

			file = new File(home + "/" + userId + ".procmail.forward");

			if (file.exists()) {
				file.delete();
			}

			file = new File(home + "/" + userId + ".vacation");

			if (file.exists()) {
				file.delete();
			}

			file = new File(home + "/" + userId + ".vacation.cache");

			if (file.exists()) {
				file.delete();
			}
		}
		catch (Exception e) {
			_log.error(e, e);
		}
	}

	public void updateBlocked(
		long companyId, long userId, List<String> blocked) {

		String home = PropsUtil.get(PropsKeys.MAIL_HOOK_CYRUS_HOME);

		File file = new File(home + "/" + userId + ".procmail.blocked");

		if ((blocked == null) || (blocked.size() == 0)) {
			file.delete();

			return;
		}

		StringBundler sb = new StringBundler(blocked.size() * 9);

		for (int i = 0; i < blocked.size(); i++) {
			String emailAddress = blocked.get(i);

			sb.append("\n");
			sb.append(":0\n");
			sb.append("* ^From.*");
			sb.append(emailAddress);
			sb.append("\n");
			sb.append("{\n");
			sb.append(":0\n");
			sb.append("/dev/null\n");
			sb.append("}\n");
		}

		try {
			FileUtil.write(file, sb.toString());
		}
		catch (Exception e) {
			_log.error(e, e);
		}
	}

	public void updateEmailAddress(
		long companyId, long userId, String emailAddress) {

		try {
			CyrusVirtualUtil.removeByUserId(userId);

			CyrusVirtual virtual = new CyrusVirtual(emailAddress, userId);

			CyrusVirtualUtil.update(virtual);
		}
		catch (Exception e) {
			_log.error(e, e);
		}
	}

	public void updatePassword(long companyId, long userId, String password) {
		CyrusUser user = null;

		try {
			user = CyrusUserUtil.findByPrimaryKey(userId);
		}
		catch (NoSuchCyrusUserException nscue) {
			user = new CyrusUser(userId, password);
		}
		catch (Exception e) {
			_log.error(e, e);
		}

		try {
			user.setPassword(password);

			CyrusUserUtil.update(user);
		}
		catch (Exception e) {
			_log.error(e, e);
		}
	}

	private static Log _log = LogFactoryUtil.getLog(CyrusHook.class);

}