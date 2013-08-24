#!/bin/bash
#
# Copyright (C) 2009-2013 FoundationDB, LLC
#
# This program is free software: you can redistribute it and/or modify
# it under the terms of the GNU Affero General Public License as published by
# the Free Software Foundation, either version 3 of the License, or
# (at your option) any later version.
#
# This program is distributed in the hope that it will be useful,
# but WITHOUT ANY WARRANTY; without even the implied warranty of
# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
# GNU Affero General Public License for more details.
#
# You should have received a copy of the GNU Affero General Public License
# along with this program.  If not, see <http://www.gnu.org/licenses/>.
#

TARGET=$(ls -d $(dirname $0)/../../../target)
BASEJAR=$(ls ${TARGET}/foundationdb-sql-layer-*.*.*-SNAPSHOT.jar)
java -cp "${BASEJAR}:${BASEJAR%.jar}-tests.jar:${TARGET}/dependency/*" com.foundationdb.sql.test.Tester "$@"
