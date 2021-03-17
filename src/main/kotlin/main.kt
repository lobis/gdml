import space.kscience.gdml.*

import java.io.File
import kotlin.io.*

enum class Chamber(val mm: Double) {
    Diameter(102.0),
}

val geometry = Gdml {
    // geometric variables
    val worldSize = 1000
    val chamber = mapOf<String, Double>(
        "height" to 30.0, "diameter" to 102.0,
        "outerSquareSide" to 134.0, "backplateThickness" to 15.0,
        "teflonWallThickness" to 11.0, "readoutKaptonThickness" to 0.5,
        "readoutCopperThickness" to 0.2, "readoutPlaneSide" to 60.0
    )

    // materials
    loadMaterialsFromUrl("https://raw.githubusercontent.com/rest-for-physics/materials/main/NIST.xml")
    val materialsMap = mapOf(
        "Copper" to materials.composite("G4_Cu"),
        "Lead" to materials.composite("G4_Pb"),
        "Teflon" to materials.composite("G4_TEFLON"),
    )

    structure {
        // world
        val worldMaterial = materials.composite("G4_AIR")
        val worldBox = solids.box(worldSize, worldSize, worldSize, "worldBox")
        // chamber

        val chamberBodySolid = solids.subtraction(
            solids.box(
                chamber["outerSquareSide"]!!,
                chamber["outerSquareSide"]!!,
                chamber["height"]!!,
                "chamberBodyBaseSolid"
            ), solids.tube(Chamber.Diameter.mm * 0.5, chamber["height"]!!, "chamberBodyHoleSolid"),
            "chamberBodySolid"
        )
        val chamberBodyVolume = volume(materialsMap["Copper"]!!, chamberBodySolid, "chamberBodyVolume")

        val chamberBackplateSolid = solids.box(
            chamber["outerSquareSide"]!!,
            chamber["outerSquareSide"]!!,
            chamber["backplateThickness"]!!,
            "chamberBackplateSolid"
        )
        val chamberBackplateVolume = volume(materialsMap["Copper"]!!, chamberBackplateSolid, "chamberBackplateVolume")

        val chamberTeflonWallSolid =
            solids.tube(chamber["diameter"]!! * 0.5, chamber["height"]!!, "chamberTeflonWallSolid") {
                rmin = chamber["diameter"]!! * 0.5 - chamber["teflonWallThickness"]!!
            }
        val chamberTeflonWallVolume =
            volume(materialsMap["Teflon"]!!, chamberTeflonWallSolid, "chamberTeflonWallVolume")

        //
        val chamberVolume = assembly {
            physVolume(chamberBackplateVolume) {
                name = "chamberBackplate"
                position {
                    z =
                        -chamber["height"]!! * 0.5 - chamber["readoutKaptonThickness"]!! - chamber["backplateThickness"]!! * 0.5
                }
            }
            physVolume(chamberBodyVolume) {
                name = "chamberBody"
            }
            physVolume(chamberTeflonWallVolume) {
                name = "chamberTeflonWall"
            }
        }

        // chamber teflon walls

        // world setup
        world = volume(worldMaterial, worldBox, "world") {

            physVolume(chamberVolume) {
                name = "chamberBody"
                position {
                    y = 200
                }
                rotation {
                    y = 0
                }
            }
        }
    }
}


fun main() {

    val gdmlString = geometry.encodeToString()
    File("Setup.gdml").writeText(gdmlString)
}