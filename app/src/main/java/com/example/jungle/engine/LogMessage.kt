package com.example.jungle.engine

/**
 * Сообщение игры.
 * @param toPlayerId кому адресовано (null — всем). Нужно для режимов с двумя игроками:
 *                   каждый видит только свои сообщения и «слышимые» события соперника.
 */
data class LogMessage(val text: String, val toPlayerId: Int? = null)
