package com.kidozh.discuzhub.interact

import com.kidozh.discuzhub.results.BaseResult
import com.kidozh.discuzhub.results.VariableResults

interface BaseStatusInteract {
    fun setBaseResult(baseVariableResult: BaseResult, variableResults: VariableResults)
}